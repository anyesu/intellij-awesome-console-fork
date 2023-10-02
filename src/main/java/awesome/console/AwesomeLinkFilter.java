package awesome.console;

import awesome.console.config.AwesomeConsoleConfig;
import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import awesome.console.util.FileUtils;
import awesome.console.util.IntegerUtil;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.HyperlinkInfoFactory;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AwesomeLinkFilter implements Filter {
	private static final Logger logger = Logger.getInstance(AwesomeLinkFilter.class);

	// JediTerm Unicode private use area U+100000–U+10FFFD
	public static final String DWC = "\uE000"; // Second part of double-width character

	public static final String REGEX_ROW_COL = "(?i:\\s*(?:,\\s*line|:\\s*\\[?|\\()\\s*(?<row>\\d+)(?:\\s*[:,](?:\\s*col(?:umn)?)?\\s*(?<col>\\d+)(?:\\s*[)\\]])?)?)?";

	public static final String REGEX_SEPARATOR = "[\\\\/]+";

	public static final String REGEX_CHAR = String.format("[^\\\\/:*?\"<>|\\s]%s?", DWC);

	public static final String REGEX_DRIVE = "(?:~|[a-zA-Z]:)?" + REGEX_SEPARATOR;

	public static final String REGEX_PROTOCOL = "[a-zA-Z]+://";

	public static final String REGEX_FILE_NAME = String.format("((?!\\(\\d+,\\d+\\)|[,;][a-zA-Z]:)(?:%s))+(?<![,;()\\]])", REGEX_CHAR);

	public static final String REGEX_FILE_NAME_WITH_SPACE = String.format("(?! )(?:(?:%s)| )+(?<! )", REGEX_CHAR);

	public static final String REGEX_PATH_WITH_SPACE = String.format(
			"\"(?<spacePath>(?<protocol1>%s)?(%s)?((%s|%s)+))\"",
			REGEX_PROTOCOL, REGEX_DRIVE, REGEX_FILE_NAME_WITH_SPACE, REGEX_SEPARATOR
	);

	public static final String REGEX_PATH = String.format(
			"(?<path>(?<protocol2>%s)?(%s)?((%s|%s)+))",
			REGEX_PROTOCOL, REGEX_DRIVE, REGEX_FILE_NAME, REGEX_SEPARATOR
	);

	public static final Pattern FILE_PATTERN = Pattern.compile(
			String.format("(?![ ,;\\]])(?<link>[\\(\\[]?(?:%s|%s)%s[\\)\\]]?)", REGEX_PATH_WITH_SPACE, REGEX_PATH, REGEX_ROW_COL),
			Pattern.UNICODE_CHARACTER_CLASS);

	public static final Pattern URL_PATTERN = Pattern.compile(
			"(?<link>[(']?(?<protocol>(([a-zA-Z]+):)?([/\\\\~]))(?<path>([-.!~*\\\\'()\\w;/?:@&=+$,%#]" + DWC + "?)+))",
			Pattern.UNICODE_CHARACTER_CLASS);

	public static final Pattern DRIVE_PATTERN = Pattern.compile(String.format("^(?<drive>%s)", REGEX_DRIVE));

	private static final int maxSearchDepth = 1;

	private final AwesomeConsoleConfig config;
	private final Map<String, List<VirtualFile>> fileCache;
	private final Map<String, List<VirtualFile>> fileBaseCache;
	private final Project project;
	private final List<String> srcRoots;
	private final ThreadLocal<Matcher> fileMatcher = ThreadLocal.withInitial(() -> FILE_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> urlMatcher = ThreadLocal.withInitial(() -> URL_PATTERN.matcher(""));
	private final ThreadLocal<Matcher> driveMatcher = ThreadLocal.withInitial(() -> DRIVE_PATTERN.matcher(""));
	private final ProjectRootManager projectRootManager;

	public AwesomeLinkFilter(final Project project) {
		this.project = project;
		this.fileCache = new ConcurrentHashMap<>();
		this.fileBaseCache = new ConcurrentHashMap<>();
		projectRootManager = ProjectRootManager.getInstance(project);
		srcRoots = getSourceRoots();
		config = AwesomeConsoleConfig.getInstance();

		createFileCache();
	}

	@Nullable
	@Override
	public Result applyFilter(final String line, final int endPoint) {
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
		final HyperlinkInfoFactory hyperlinkInfoFactory = HyperlinkInfoFactory.getInstance();

		final List<FileLinkMatch> matches = detectPaths(line);

		for(final FileLinkMatch match: matches) {
			File file = resolveFile(match.path);
			if (null != file && (isExternal(file) || file.isDirectory())) {
				if (FileUtils.quickExists(file.getAbsolutePath())) {
					results.add(
							new Result(
									startPoint + match.start,
									startPoint + match.end,
									new OpenUrlHyperlinkInfo(file.getAbsolutePath()))
					);
				}
				continue;
			} else if (null != file) {
				match.path = file.getAbsolutePath();
			}

			String path = PathUtil.getFileName(match.path);
			if (path.endsWith("$")) {
				path = path.substring(0, path.length() - 1);
			}
			List<VirtualFile> matchingFiles = fileCache.get(path);

			if (null == matchingFiles) {
				matchingFiles = getResultItemsFileFromBasename(path);
				if (null == matchingFiles || matchingFiles.isEmpty()) {
					continue;
				}
			}

			if (matchingFiles.isEmpty()) {
				continue;
			}

			final List<VirtualFile> bestMatchingFiles = findBestMatchingFiles(match, matchingFiles);
			if (bestMatchingFiles != null && !bestMatchingFiles.isEmpty()) {
				matchingFiles = bestMatchingFiles;
			}

			final int row = match.linkedRow <= 0 ? 0 : match.linkedRow - 1;
			final HyperlinkInfo linkInfo = hyperlinkInfoFactory.createMultipleFilesHyperlinkInfo(
					matchingFiles,
					row,
					project,
					(project, psiFile, editor, originalEditor) -> editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(row, match.linkedCol))
			);

			results.add(new Result(
					startPoint + match.start,
					startPoint + match.end,
					linkInfo)
			);
		}

		return results;
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

	private void createFileCache() {
		projectRootManager.getFileIndex().iterateContent(
				new AwesomeProjectFilesIterator(fileCache, fileBaseCache));
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

	private boolean isSurroundedBy(@NotNull final String s, @NotNull final String[] pairs) {
		for (final String pair : pairs) {
			if (s.startsWith(String.valueOf(pair.charAt(0))) && s.endsWith(String.valueOf(pair.charAt(1)))) {
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
			String path = fileMatcher.group("spacePath");
			if (null == path) {
				path = fileMatcher.group("path");
			}
			if (null == path) {
				logger.error("Regex group 'path' was NULL while trying to match path line: " + line + "\nfor match: " + match);
				continue;
			}

			if ("/".equals(path) || "\\".equals(path)) {
				// ignore the root directory as it matches a lot
				continue;
			}

			String protocol = fileMatcher.group("protocol1");
			if (null == protocol) {
				protocol = fileMatcher.group("protocol2");
			}
			if ("file://".equalsIgnoreCase(protocol)) {
				match = match.replace(protocol, "");
				path = path.substring(protocol.length());
			} else if (null != protocol) {
				// ignore url
				continue;
			}
			final int row = IntegerUtil.parseInt(fileMatcher.group("row")).orElse(0);
			final int col = IntegerUtil.parseInt(fileMatcher.group("col")).orElse(0);
			match = decodeDwc(match);
			int offset = 0;
			if (isSurroundedBy(match, new String[]{"()", "[]"})) {
				match = match.substring(1, match.length() - 1);
				offset = 1;
			}
			results.add(new FileLinkMatch(
					match, decodeDwc(path),
					fileMatcher.start() + offset,
					fileMatcher.end() - offset,
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
}
