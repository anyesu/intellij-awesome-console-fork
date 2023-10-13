package awesome.console;

import awesome.console.config.AwesomeConsoleStorage;
import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import awesome.console.util.FileUtils;
import awesome.console.util.HyperlinkUtils;
import awesome.console.util.IntegerUtil;
import awesome.console.util.Notifier;
import awesome.console.util.RegexUtils;
import awesome.console.util.SystemUtils;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.util.PathUtil;
import com.intellij.util.messages.MessageBusConnection;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * allow Filter to run in dumb mode (when indices are in background update)
 */
public class AwesomeLinkFilter implements Filter, DumbAware {
	private static final Logger logger = Logger.getInstance(AwesomeLinkFilter.class);

	// JediTerm Unicode private use area U+100000–U+10FFFD
	public static final String DWC = "\uE000"; // Second part of double-width character

	public static final String REGEX_ROW_COL = "(?i:\\s*(?:[:,]\\s*line|:\\s*\\[?|(?=\\(\\s*\\d+\\s*[:,]\\s*\\d+\\s*\\))\\()\\s*(?<row>\\d+)(?:\\s*[:,](?:\\s*col(?:umn)?)?\\s*(?<col>\\d+)(?:\\s*[)\\]])?)?)?";

	public static final String REGEX_SEPARATOR = "[\\\\/]+";

	public static final String REGEX_CHAR = String.format("[^\\\\/:*?\"<>|\\s]%s?", DWC);

	public static final String REGEX_DRIVE = "(?:~|[a-zA-Z]:)?" + REGEX_SEPARATOR;

	public static final String REGEX_PROTOCOL = "[a-zA-Z]+://";

	public static final String REGEX_FILE_NAME = String.format("((?!\\(\\d+,\\d+\\)|\\(\\S+\\.(java|kts?):\\d+\\)|[,;][a-zA-Z]:)(?:%s))+(?<![,;()\\]'])", REGEX_CHAR);

	public static final String REGEX_FILE_NAME_WITH_SPACE = String.format("(?! )(?:(?:%s)| )+(?<! )", REGEX_CHAR);

	public static final String REGEX_PATH_WITH_SPACE = String.format(
			"\"(?<spacePath>(?<protocol1>%s)?(%s)?(((?![a-zA-Z]:)%s|%s)+))\"",
			REGEX_PROTOCOL, REGEX_DRIVE, REGEX_FILE_NAME_WITH_SPACE, REGEX_SEPARATOR
	);

	public static final String REGEX_PATH = String.format(
			"(?<path>(?<protocol2>%s)?(%s)?(((?![a-zA-Z]:)%s|%s)+))",
			REGEX_PROTOCOL, REGEX_DRIVE, REGEX_FILE_NAME, REGEX_SEPARATOR
	);

	public static final Pattern FILE_PATTERN = Pattern.compile(
			String.format("(?![ ,;\\]])(?<link>[\\(\\[']?(?:%s|%s)%s[)\\]']?)", REGEX_PATH_WITH_SPACE, REGEX_PATH, REGEX_ROW_COL),
			Pattern.UNICODE_CHARACTER_CLASS);

	public static final Pattern URL_PATTERN = Pattern.compile(
			"(?<link>[(']?(?<protocol>(([a-zA-Z]+):)?([/\\\\~]))(?<path>([-.!~*\\\\'()\\w;/?:@&=+$,%#]" + DWC + "?)+))",
			Pattern.UNICODE_CHARACTER_CLASS);

	public static final Pattern DRIVE_PATTERN = Pattern.compile(String.format("^(?<drive>%s)", REGEX_DRIVE));

	public static final Pattern STACK_TRACE_ELEMENT_PATTERN = Pattern.compile("^[\\w|\\s]*at\\s+(.+)\\.(.+)\\((.+\\.(java|kts?)):(\\d+)\\)");

	private static final int maxSearchDepth = 1;

	private final AwesomeConsoleStorage config;
	private final Map<String, List<VirtualFile>> fileCache;
	private final Map<String, List<VirtualFile>> fileBaseCache;
	private final Project project;
	private volatile List<String> srcRoots = Collections.emptyList();
	private final ThreadLocal<Matcher> fileMatcher = ThreadLocal.withInitial(() -> FILE_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> urlMatcher = ThreadLocal.withInitial(() -> URL_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> driveMatcher = ThreadLocal.withInitial(() -> DRIVE_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> stackTraceElementMatcher = ThreadLocal.withInitial(() -> STACK_TRACE_ELEMENT_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> ignoreMatcher = new ThreadLocal<>();
	private final ProjectRootManager projectRootManager;

	private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

	private final ReentrantReadWriteLock.ReadLock cacheReadLock = cacheLock.readLock();

	private final ReentrantReadWriteLock.WriteLock cacheWriteLock = cacheLock.writeLock();

	private final AwesomeProjectFilesIterator indexIterator;

	private volatile boolean cacheInitialized = false;

	public AwesomeLinkFilter(final Project project) {
		this.project = project;
		this.fileCache = new ConcurrentHashMap<>();
		this.fileBaseCache = new ConcurrentHashMap<>();
		this.indexIterator = new AwesomeProjectFilesIterator(fileCache, fileBaseCache);
		projectRootManager = ProjectRootManager.getInstance(project);
		config = AwesomeConsoleStorage.getInstance();

		createFileCache();
	}

	@Nullable
	@Override
	public Result applyFilter(@NotNull final String line, final int endPoint) {
		if (!shouldFilter(line)) {
			return null;
		}

		prepareFilter();

		final List<ResultItem> results = new ArrayList<>();
		final int startPoint = endPoint - line.length();
		final List<String> chunks = splitLine(line);
		int offset = 0;

		for (final String chunk : chunks) {
			results.addAll(getResultItemsFile(chunk, startPoint + offset));
			if (config.SEARCH_URLS) {
				results.addAll(getResultItemsUrl(chunk, startPoint + offset));
			}
			offset += chunk.length();
		}

		return new Result(results);
	}

	private boolean shouldFilter(@NotNull final String line) {
		final Matcher stackTraceElementMatcher = this.stackTraceElementMatcher.get();
		if (stackTraceElementMatcher.reset(line).find()) {
			// Ignore handling java stackTrace as ExceptionFilter does well
			return false;
		}
		return true;
	}

	private void prepareFilter() {
		prepareIgnoreMatcher();
	}

	private void prepareIgnoreMatcher() {
		final Matcher ignoreMatcher = this.ignoreMatcher.get();
		final Matcher ignoreMatcherConfig = config.ignoreMatcher;
		if (!Objects.equals(ignoreMatcher, ignoreMatcherConfig)) {
			this.ignoreMatcher.set(ignoreMatcherConfig);
		}
	}

	/**
	 * ref: https://github.com/JetBrains/jediterm/commit/5a05fe18a1a3475a157dbdda6448f682678f55fb#diff-0065f89b4f46c30f15e7ca66d3626b43b41f8c30c9d064743304fe8304186a06R1036-R1039
	 */
	private String decodeDwc(@NotNull final String s) {
		return s.replace(DWC, "");
	}

	public List<String> splitLine(final String line) {
		final List<String> chunks = new ArrayList<>();
		final int length = line.length();
		if (!config.LIMIT_LINE_LENGTH || config.LINE_MAX_LENGTH >= length) {
			chunks.add(line);
			return chunks;
		}
		if (!config.SPLIT_ON_LIMIT) {
			chunks.add(line.substring(0, config.LINE_MAX_LENGTH));
			return chunks;
		}
		int offset = 0;
		do {
			final String chunk = line.substring(offset, Math.min(length, offset + config.LINE_MAX_LENGTH));
			chunks.add(chunk);
			offset += config.LINE_MAX_LENGTH;
		} while (offset < length - 1);
		return chunks;
	}

	public List<ResultItem> getResultItemsUrl(final String line, final int startPoint) {
		final List<ResultItem> results = new ArrayList<>();
		final List<URLLinkMatch> matches = detectURLs(line);

		for (final URLLinkMatch match : matches) {
			if (shouldIgnore(match.match)) {
				continue;
			}

			final String file = getFileFromUrl(match.match);

			if (null != file && !FileUtils.quickExists(file)) {
				continue;
			}
			results.add(
					new Result(
							startPoint + match.start,
							startPoint + match.end,
							new OpenUrlHyperlinkInfo(match.match))
			);
		}
		return results;
	}

	private boolean isAbsolutePath(@NotNull final String path) {
		final Matcher driveMatcher = this.driveMatcher.get();
		return driveMatcher.reset(path).find();
	}

	public String getFileFromUrl(@NotNull final String url) {
		if (isAbsolutePath(url)) {
			return url;
		}
		final String fileUrl = "file://";
		if (url.startsWith(fileUrl)) {
			return url.substring(fileUrl.length());
		}
		return null;
	}

	private File resolveFile(@NotNull String path) {
		if (FileUtils.isUncPath(path)) {
			return null;
		}
		String basePath = isAbsolutePath(path) ? null : project.getBasePath();
		try {
			// if basePath is null, path is assumed to be absolute.
			return new File(new File(basePath, path).getCanonicalPath());
		} catch (IOException e) {
			logger.error(String.format("Unable to resolve file path: \"%s\" with basePath \"%s\"", path, basePath));
			logger.error(e);
			return null;
		}
	}

	private boolean isExternal(@NotNull File file) {
		String basePath = project.getBasePath();
		if (null == basePath) {
			return false;
		}
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		return !generalizePath(file.getAbsolutePath()).startsWith(basePath);
	}

	public List<ResultItem> getResultItemsFile(final String line, final int startPoint) {
		final List<ResultItem> results = new ArrayList<>();

		final List<FileLinkMatch> matches = detectPaths(line);

		for(final FileLinkMatch match: matches) {
			if (shouldIgnore(match.match)) {
				continue;
			}

			File file = resolveFile(match.path);
			if (null != file) {
				final boolean isExternal = isExternal(file);
				final boolean exists = file.exists();
				String filePath = file.getAbsolutePath();
				if (exists) {
					final HyperlinkInfo linkInfo = HyperlinkUtils.buildFileHyperlinkInfo(project, filePath, match.linkedRow, match.linkedCol);
					results.add(new Result(startPoint + match.start, startPoint + match.end, linkInfo));
					continue;
				} else if (isExternal) {
					if (!match.path.startsWith("/") && !match.path.startsWith("\\")) {
						continue;
					}
					// Resolve absolute paths starting with a slash into relative paths based on the project root as a fallback
					filePath = new File(project.getBasePath(), match.path).getAbsolutePath();
				}
				match.path = getRelativePath(filePath);
			}

			String path = PathUtil.getFileName(match.path);
			if (path.endsWith("$")) {
				path = path.substring(0, path.length() - 1);
			}

			List<VirtualFile> matchingFiles;
			cacheReadLock.lock();
			try {
				matchingFiles = fileCache.get(path);
				if (null == matchingFiles) {
					matchingFiles = getResultItemsFileFromBasename(path);
				}
				if (null != matchingFiles) {
					// Don't use parallelStream because `shouldIgnore` uses ThreadLocal
					matchingFiles = matchingFiles.stream()
												 .filter(f -> !shouldIgnore(getRelativePath(f.getPath())))
												 .collect(Collectors.toList());
				}
			} finally {
				cacheReadLock.unlock();
			}

			if (null == matchingFiles || matchingFiles.isEmpty()) {
				continue;
			}

			final List<VirtualFile> bestMatchingFiles = findBestMatchingFiles(match, matchingFiles);
			if (bestMatchingFiles != null && !bestMatchingFiles.isEmpty()) {
				matchingFiles = bestMatchingFiles;
			}

			final HyperlinkInfo linkInfo = HyperlinkUtils.createMultipleFilesHyperlinkInfo(
					matchingFiles,
					match.linkedRow, match.linkedCol,
					project, config.fixChooseTargetFile
			);

			results.add(new Result(
					startPoint + match.start,
					startPoint + match.end,
					linkInfo)
			);
		}

		return results;
	}

	private String getRelativePath(@NotNull String path) {
		path = generalizePath(path);
		String basePath = project.getBasePath();
		if (null == basePath) {
			return path;
		}
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		return path.startsWith(basePath) ? path.substring(basePath.length()) : path;
	}

	private List<VirtualFile> findBestMatchingFiles(final FileLinkMatch match, final List<VirtualFile> matchingFiles) {
		return findBestMatchingFiles(generalizePath(match.path), matchingFiles);
	}

	private List<VirtualFile> findBestMatchingFiles(final String generalizedMatchPath,
			final List<VirtualFile> matchingFiles) {
		final List<VirtualFile> foundFiles = getFilesByPath(generalizedMatchPath, matchingFiles);
		if (!foundFiles.isEmpty()) {
			return foundFiles;
		}
		final String widerMetchingPath = dropOneLevelFromRoot(generalizedMatchPath);
		if (widerMetchingPath != null) {
			return findBestMatchingFiles(widerMetchingPath, matchingFiles);
		}
		return null;
	}

	private List<VirtualFile> getFilesByPath(final String generalizedMatchPath, final List<VirtualFile> matchingFiles) {
		return matchingFiles.parallelStream()
				.filter(file -> generalizePath(file.getPath()).endsWith(generalizedMatchPath))
				.collect(Collectors.toList());
	}

	private String dropOneLevelFromRoot(final String path) {
		if (path.contains("/")) {
			return path.substring(path.indexOf('/')+1);
		} else {
			return null;
		}
	}

	private String generalizePath(final String path) {
		return path.replace('\\', '/');
	}

	public List<VirtualFile> getResultItemsFileFromBasename(final String match) {
		return getResultItemsFileFromBasename(match, 0);
	}

	public List<VirtualFile> getResultItemsFileFromBasename(final String match, final int depth) {
		final char packageSeparator = '.';
		final int index = match.lastIndexOf(packageSeparator);
		if (-1 >= index) {
			return new ArrayList<>();
		}
		final String basename = match.substring(index + 1);
		final String origin = match.substring(0, index);
		final String path = origin.replace(packageSeparator, File.separatorChar);
		if (0 >= basename.length()) {
			return new ArrayList<>();
		}
		if (!fileBaseCache.containsKey(basename)) {
			/* Try to search deeper down the rabbit hole */
			if (depth <= maxSearchDepth) {
				return getResultItemsFileFromBasename(origin, depth + 1);
			}
			return new ArrayList<>();
		}

		return fileBaseCache.get(basename).parallelStream()
				.filter(file -> null != file.getParent())
				.filter(file -> matchSource(file.getParent().getPath(), path))
				.collect(Collectors.toList());
	}

	private void notifyUser(String title, String message) {
		Notifier.notify(
				project, title, message,
				NotificationAction.createSimple("Reload file cache", () -> reloadFileCache("manual"))
		);
	}

	private void reloadFileCache(String reason) {
		cacheWriteLock.lock();
		try {
			srcRoots = getSourceRoots();
			fileCache.clear();
			fileBaseCache.clear();
			projectRootManager.getFileIndex().iterateContent(indexIterator);
			String state = cacheInitialized ? "reload" : "init";
			if (!cacheInitialized || config.DEBUG_MODE) {
				notifyUser(
						String.format("%s file cache ( %s )", state, reason),
						String.format("fileCache[%d], fileBaseCache[%d]", fileCache.size(), fileBaseCache.size())
				);
			}
			if (!cacheInitialized) {
				cacheInitialized = true;
			}
			logger.info(String.format(
					"project[%s]: %s file cache ( %s ): fileCache[%d], fileBaseCache[%d]",
					project.getName(), state, reason, fileCache.size(), fileBaseCache.size()
			));
		} finally {
			cacheWriteLock.unlock();
		}
	}

	private void createFileCache() {
		reloadFileCache("open project");

		MessageBusConnection connection = project.getMessageBus().connect();

		// DumbService.smartInvokeLater() is executed only once,
		// but exitDumbMode will be executed every time the mode changes.
		connection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
			@Override
			public void exitDumbMode() {
				reloadFileCache("indices are updated");
			}
		});

		// VFS listeners are application level and will receive events for changes happening in
		// all the projects opened by the user. You may need to filter out events that aren't
		// relevant to your task (e.g., via ProjectFileIndex.isInContent()).
		// ref: https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html#virtual-file-system-events
		// ref: https://plugins.jetbrains.com/docs/intellij/virtual-file.html#how-do-i-get-notified-when-vfs-changes
		connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
			@Override
			public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
				List<VirtualFile> newFiles = new ArrayList<>();
				boolean deleteFile = false;

				for (VFileEvent event : events) {
					final VirtualFile file = event.getFile();
					if (null == file || !isInContent(file, event instanceof VFileDeleteEvent)) {
						continue;
					}
					if (event instanceof VFileCopyEvent) {
						newFiles.add(((VFileCopyEvent) event).findCreatedFile());
					} else if (event instanceof VFileCreateEvent) {
						newFiles.add(file);
					} else if (event instanceof VFileDeleteEvent) {
						deleteFile = true;
					} else if (event instanceof VFileMoveEvent) {
						// No processing is required since the file name has not changed and
						// the path to the virtual file will be updated automatically
					} else if (event instanceof VFilePropertyChangeEvent) {
						final VFilePropertyChangeEvent pce = (VFilePropertyChangeEvent) event;
						// Rename file
						if (VirtualFile.PROP_NAME.equals(pce.getPropertyName())
								&& !Objects.equals(pce.getNewValue(), pce.getOldValue())) {
							deleteFile = true;
							newFiles.add(file);
						}
					}
				}

				if (newFiles.isEmpty() && !deleteFile) {
					return;
				}

				cacheWriteLock.lock();
				try {
					// Since there is only one event for deleting a directory, simply clean up all the invalid files
					if (deleteFile) {
						fileCache.forEach((key, value) -> value.removeIf(it -> !it.isValid() || !key.equals(it.getName())));
						fileBaseCache.forEach((key, value) -> value.removeIf(it -> !it.isValid() || !key.equals(it.getNameWithoutExtension())));
					}
					newFiles.forEach(indexIterator::processFile);
					logger.info(String.format("project[%s]: flush file cache", project.getName()));
				} finally {
					cacheWriteLock.unlock();
				}
			}
		});
	}

	private boolean isInContent(@NotNull VirtualFile file, boolean isDelete) {
		if (isDelete) {
			String basePath = project.getBasePath();
			if (null == basePath) {
				// Default project. Unlikely to happen.
				return false;
			}
			if (!basePath.endsWith("/")) {
				basePath += "/";
			}
			return file.getPath().startsWith(basePath);
		}
		return projectRootManager.getFileIndex().isInContent(file);
	}

	private List<String> getSourceRoots() {
		final VirtualFile[] contentSourceRoots = projectRootManager.getContentSourceRoots();
		return Arrays.stream(contentSourceRoots).map(VirtualFile::getPath).collect(Collectors.toList());
	}

	private boolean matchSource(final String parent, final String path) {
		for (final String srcRoot : srcRoots) {
			if (generalizePath(srcRoot + File.separatorChar + path).equals(parent)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSurroundedBy(@NotNull final String s, @NotNull final String[] pairs, int[] offsets) {
		if (s.length() < 2) {
			return false;
		}
		for (final String pair : pairs) {
			final String start = String.valueOf(pair.charAt(0));
			final String end = String.valueOf(pair.charAt(1));
			if (s.startsWith(start)) {
				offsets[0] = 1;
				offsets[1] = s.endsWith(end) ? 1 : 0;
				return true;
			} else if (s.endsWith(end) && !s.substring(0, s.length() - 1).contains(start)) {
				offsets[0] = 0;
				offsets[1] = 1;
				return true;
			}
		}
		return false;
	}

	@NotNull
	public List<FileLinkMatch> detectPaths(@NotNull final String line) {
		final Matcher fileMatcher = this.fileMatcher.get();
		fileMatcher.reset(line);
		final List<FileLinkMatch> results = new LinkedList<>();
		while (fileMatcher.find()) {
			String match = fileMatcher.group("link");
			String path = RegexUtils.matchGroup(fileMatcher, "spacePath", "path");
			if (null == path) {
				logger.error("Regex group 'path' was NULL while trying to match path line: " + line + "\nfor match: " + match);
				continue;
			}

			final String protocol = RegexUtils.matchGroup(fileMatcher, "protocol1", "protocol2");
			if ("file://".equalsIgnoreCase(protocol)) {
				match = match.replace(protocol, "");
				path = path.substring(protocol.length());
			} else if (null != protocol) {
				// ignore url
				continue;
			}

			// Resolve '~' to user's home directory
			if ("~".equals(path)) {
				path = SystemUtils.getUserHome();
			} else if (path.startsWith("~/") || path.startsWith("~\\")) {
				path = SystemUtils.getUserHome() + path.substring(1);
			}

			final int row = IntegerUtil.parseInt(fileMatcher.group("row")).orElse(0);
			final int col = IntegerUtil.parseInt(fileMatcher.group("col")).orElse(0);
			match = decodeDwc(match);
			int[] offsets = new int[]{0, 0};
			if (isSurroundedBy(match, new String[]{"()", "[]", "''"}, offsets)) {
				match = match.substring(offsets[0], match.length() - offsets[1]);
			}
			results.add(new FileLinkMatch(
					match, decodeDwc(path),
					fileMatcher.start() + offsets[0],
					fileMatcher.end() - offsets[1],
					row, col
			));
		}
		return results;
	}

	@NotNull
	public List<URLLinkMatch> detectURLs(@NotNull final String line) {
		final Matcher urlMatcher = this.urlMatcher.get();
		urlMatcher.reset(line);
		final List<URLLinkMatch> results = new LinkedList<>();
		while (urlMatcher.find()) {
			String match = urlMatcher.group("link");
			if (null == match) {
				logger.error("Regex group 'link' was NULL while trying to match url line: " + line);
				continue;
			}

			match = decodeDwc(match);

			int startOffset = 0;
			int endOffset = 0;

			for (final String surrounding : new String[]{"()", "''"}) {
				final String start = "" + surrounding.charAt(0);
				final String end = "" + surrounding.charAt(1);
				if (match.startsWith(start)) {
					startOffset = 1;
					match = match.substring(1);
					if (match.endsWith(end)) {
						endOffset = 1;
						match = match.substring(0, match.length() - 1);
					}
				}
			}
			results.add(new URLLinkMatch(match, urlMatcher.start() + startOffset, urlMatcher.end() - endOffset));
		}
		return results;
	}

	private boolean shouldIgnore(@NotNull final String match) {
		final Matcher ignoreMatcher = this.ignoreMatcher.get();
		return config.isUseIgnorePattern() && null != ignoreMatcher && ignoreMatcher.reset(match).find();
	}
}
