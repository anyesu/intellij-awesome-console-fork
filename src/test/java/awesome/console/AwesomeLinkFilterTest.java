package awesome.console;

import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.stream.Collectors;
import org.junit.Test;

import java.util.List;

public class AwesomeLinkFilterTest extends BasePlatformTestCase {
	@Test
	public void testFileWithoutDirectory() {
		assertPathDetection("Just a file: test.txt", "test.txt");
	}

	@Test
	public void testFileContainingSpecialCharsWithoutDirectory() {
		assertPathDetection("Another file: _test.txt", "_test.txt");
		assertPathDetection("Another file: test-me.txt", "test-me.txt");
	}

	@Test
	public void testSimpleFileWithLineNumberAndColumn() {
		assertPathDetection("With line: file1.java:5:5", "file1.java:5:5", 5, 5);
	}

	@Test
	public void testFileInHomeDirectory() {
		assertPathDetection("Another file: ~/testme.txt", "~/testme.txt");
	}

	@Test
	public void testFileContainingDotsWithoutDirectory() {
		assertPathDetection("Just a file: t.es.t.txt", "t.es.t.txt");
	}

	@Test
	public void testFileInRelativeDirectoryUnixStyle() {
		assertPathDetection("File in a dir (unix style): subdir/test.txt pewpew", "subdir/test.txt");
	}

	@Test
	public void testFileInRelativeDirectoryWindowsStyle() {
		assertPathDetection("File in a dir (Windows style): subdir\\test.txt pewpew", "subdir\\test.txt");
	}

	@Test
	public void testFileInAbsoluteDirectoryWindowsStyleWithDriveLetter() {
		assertPathDetection("File in a absolute dir (Windows style): D:\\subdir\\test.txt pewpew", "D:\\subdir\\test.txt");
	}

	@Test
	public void testFileInAbsoluteDirectoryMixedStyleWithDriveLetter() {
		assertPathDetection("Mixed slashes: D:\\test\\me/test.txt - happens stometimes", "D:\\test\\me/test.txt");
	}

	@Test
	public void testFileInRelativeDirectoryWithLineNumber() {
		assertPathDetection("With line: src/test.js:55", "src/test.js:55", 55);
	}

	@Test
	public void testFileInRelativeDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbers() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: src\\api\\service.ts(29,50)", "src\\api\\service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileInAbsoluteDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbers() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: D:\\src\\api\\service.ts(29,50)", "D:\\src\\api\\service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileInAbsoluteDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbersAndMixedSlashes() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: D:\\src\\api/service.ts(29,50)", "D:\\src\\api/service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileWithJavaExtensionInAbsoluteDirectoryAndLineNumbersWindowsStyle() {
		assertPathDetection("Windows: d:\\my\\file.java:150", "d:\\my\\file.java:150", 150);
	}


	@Test
	public void testFileWithJavaExtensionInAbsoluteDirectoryWithLineAndColumnNumbersInMaven()
	{
		assertPathDetection("/home/me/project/run.java:[245,15]", "/home/me/project/run.java:[245,15]", 245, 15);
	}

	@Test
	public void testFileWithJavaScriptExtensionInAbsoluteDirectoryWithLineNumbers() {
		// JS exception
		assertPathDetection("bla-bla /home/me/project/run.js:27 something", "/home/me/project/run.js:27", 27);
	}

	@Test
	public void testFileWithJavaStyleExceptionClassAndLineNumbers() {
		// Java exception stack trace
		assertPathDetection("bla-bla at (AwesomeLinkFilter.java:150) something", "AwesomeLinkFilter.java:150", 150);
	}

	@Test
	public void testFileWithRelativeDirectoryPythonExtensionAndLineNumberPlusColumn() {
		assertPathDetection("bla-bla at ./foobar/AwesomeConsole.py:1337:42 something", "./foobar/AwesomeConsole.py:1337:42", 1337, 42);
	}

	@Test
	public void testFileWithoutExtensionInRelativeDirectory() {
		// detect files without extension
		assertPathDetection("No extension: bin/script pewpew", "bin/script");
		assertPathDetection("No extension: testfile", "testfile");
	}

	@Test
	public void test_unicode_path_filename() {
		assertPathDetection("unicode 中.txt yay", "中.txt");
	}

	@Test
	public void testURLHTTP() {
		assertURLDetection("omfg something: http://xkcd.com/ yay", "http://xkcd.com/");
	}

	@Test
	public void testURLHTTPWithIP() {
		assertURLDetection("omfg something: http://8.8.8.8/ yay", "http://8.8.8.8/");
	}

	@Test
	public void testURLHTTPS() {
		assertURLDetection("omfg something: https://xkcd.com/ yay", "https://xkcd.com/");
	}

	@Test
	public void testURLHTTPWithoutPath() {
		assertURLDetection("omfg something: http://xkcd.com yay", "http://xkcd.com");
	}

	@Test
	public void testURLFTPWithPort() {
		assertURLDetection("omfg something: ftp://8.8.8.8:2424 yay", "ftp://8.8.8.8:2424");
	}

	@Test
	public void testURLGIT() {
		assertURLDetection("omfg something: git://8.8.8.8:2424 yay", "git://8.8.8.8:2424");
	}

	@Test
	public void testURLFILEWithoutSchemeUnixStyle() {
		assertURLDetection("omfg something: /root/something yay", "/root/something");
	}

	@Test
	public void testURLFILEWithoutSchemeWindowsStyle() {
		assertURLDetection("omfg something: C:\\root\\something.java yay", "C:\\root\\something.java");
	}

	@Test
	public void testURLFILEWithoutSchemeWindowsStyleWithMixedSlashes() {
		assertURLDetection("omfg something: C:\\root/something.java yay", "C:\\root/something.java");
	}

	@Test
	public void testURLFILE() {
		assertURLDetection("omfg something: file:///home/root yay", "file:///home/root");
		assertPathDetection("omfg something: file:///home/root yay", "/home/root");
	}

	@Test
	public void testURLFTPWithUsernameAndPath() {
		assertURLDetection("omfg something: ftp://user:password@xkcd.com:1337/some/path yay", "ftp://user:password@xkcd.com:1337/some/path");
	}

	@Test
	public void testURLInsideBrackets() {
		assertURLDetection("something (C:\\root\\something.java) blabla", "C:\\root\\something.java");
	}

	@Test
	public void testWindowsDirectoryBackwardSlashes() {
		assertPathDetection("C:/Windows/Temp/test.tsx:5:3", "C:/Windows/Temp/test.tsx:5:3", 5, 3);
	}

	@Test
	public void testOverlyLongRowAndColumnNumbers() {
		assertPathDetection("test.tsx:123123123123123:12312312312312321", "test.tsx:123123123123123:12312312312312321", 0, 0);
	}

	@Test
	public void testTSCErrorMessages() {
		assertPathDetection("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:", "C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6", 1930, 6);
		assertURLDetection("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:", "C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:");
	}

	@Test
	public void testPythonTracebackWithQuotes() {
		assertPathDetection("File \"/Applications/plugins/python-ce/helpers/pycharm/teamcity/diff_tools.py\", line 38", "\"/Applications/plugins/python-ce/helpers/pycharm/teamcity/diff_tools.py\", line 38",38);
	}

	@Test
	public void testAngularJSAtModule() {
		assertPathDetection("src/app/@app/app.module.ts:42:5", "src/app/@app/app.module.ts:42:5",42, 5);
	}

	@Test
	public void testCsharpStacktrace() {
		assertPathDetection(
				"at Program.<Main>$(String[] args) in H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:line 4",
				"H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:line 4",
				4
		);
	}

	@Test
	public void testJavaStacktrace() {
		assertPathDetection("at Build_gradle.<init>(build.gradle.kts:9)", "build.gradle.kts:9", 9);
		assertPathDetection(
				"at awesome.console.AwesomeLinkFilterTest.testFileWithoutDirectory(AwesomeLinkFilterTest.java:14)",
				"awesome.console.AwesomeLinkFilterTest.testFileWithoutDirectory",
				"AwesomeLinkFilterTest.java:14"
		);
		assertPathDetection(
				"at redis.clients.jedis.util.Pool.getResource(Pool.java:59) ~[jedis-3.0.0.jar:?]",
				"redis.clients.jedis.util.Pool.getResource",
				"Pool.java:59"
		);
	}

	@Test
	public void testPathColonAtTheEnd() {
		assertPathDetection("colon at the end: resources/file1.java:5:1:", "resources/file1.java:5:1", 5, 1);
		assertPathDetection("colon at the end: C:\\integration\\file1.java:5:4:", "C:\\integration\\file1.java:5:4", 5, 4);
	}

	@Test
	public void testLineNumberAndColumnWithVariableWhitespace() {
		assertPathDetection("With line: file1.java: 5  :   5 ", "file1.java: 5  :   5", 5, 5);
		assertPathDetection("With line: src/test.js:  55   ", "src/test.js:  55", 55);
		assertPathDetection("From stack trace: src\\api\\service.ts( 29  ,   50 )  ", "src\\api\\service.ts( 29  ,   50 )", 29, 50);
		assertPathDetection("/home/me/project/run.java:[ 245  ,   15  ] ", "/home/me/project/run.java:[ 245  ,   15  ]", 245, 15);
		assertPathDetection("bla-bla at (AwesomeLinkFilter.java:  150) something", "AwesomeLinkFilter.java:  150", 150);
		assertPathDetection(
				"at Program.<Main>$(String[] args) in H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:    line    4    ",
				"H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:    line    4",
				4
		);
	}

	@Test
	public void testPathWithSingleDotOrDoubleDot() {
		assertPathDetection("Path: . ", ".");
		assertPathDetection("Path: .. ", "..");
		assertPathDetection("File: .gitignore ", ".gitignore");
		assertPathDetection("File ./src/test/resources/subdir/./file1.java", "./src/test/resources/subdir/./file1.java");
		assertPathDetection("File ./src/test/resources/subdir/../file1.java", "./src/test/resources/subdir/../file1.java");
	}

	@Test
	public void testUncPath() {
		assertPathDetection("UNC path: \\\\localhost\\c$", "\\\\localhost\\c$");
		assertPathDetection("UNC path: \\\\server\\share\\folder\\myfile.txt", "\\\\server\\share\\folder\\myfile.txt");
		assertPathDetection("UNC path: file://///localhost/c$", "///localhost/c$");
	}

	@Test
	public void testPathWithQuotes() {
		assertPathDetection("Path: src/test/resources/中文 空格.txt ", "空格.txt");
		assertPathDetection("Path: \"C:\\Program Files (x86)\\Windows NT\" ", "\"C:\\Program Files (x86)\\Windows NT\"");
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt\" ", "\"src/test/resources/中文 空格.txt\"");
		assertPathDetection("path: \"file://src/test/resources/中文 空格.txt\" ", "\"src/test/resources/中文 空格.txt\"");
		assertPathDetection("Path: \"  src/test/resources/中文 空格.txt  \" ", "空格.txt");
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt\":5:4 ", "\"src/test/resources/中文 空格.txt\":5:4");
		assertPathDetection("Path: \"src/test/resources/subdir/file1.java\" ", "\"src/test/resources/subdir/file1.java\"");
		assertPathDetection("Path: \"src/test/  resources/subdir/file1.java\" ", "resources/subdir/file1.java");
		assertPathDetection("Path: \"src/test/resources/subdir/ file1.java\" ", "file1.java");
		assertPathDetection("Path: \"src/test/resources/subdir /file1.java\" ", "/file1.java");
	}

	@Test
	public void testPathSeparatedByCommaOrSemicolon() {
		assertPathDetection(
				"Comma or semicolon separated paths: C:\\integration\\file1.java,C:\\integration\\file2.java;C:\\integration\\file3.java",
				"C:\\integration\\file1.java",
				"C:\\integration\\file2.java",
				"C:\\integration\\file3.java"
		);
		assertPathDetection(
				"Comma or semicolon separated paths: C:\\integration\\file1.java:20:1,C:\\integration\\file2.java:20:2;C:\\integration\\file3.java:20:3",
				"C:\\integration\\file1.java:20:1",
				"C:\\integration\\file2.java:20:2",
				"C:\\integration\\file3.java:20:3"
		);
		assertPathDetection(
				"Comma or semicolon separated paths: /tmp/file1.java,/tmp/file2.java;/tmp/file3.java",
				"/tmp/file1.java",
				"/tmp/file2.java",
				"/tmp/file3.java"
		);
		assertPathDetection(
				"Comma or semicolon separated paths: /tmp/file1.java:20:1,/tmp/file2.java:20:2;/tmp/file3.java:20:3",
				"/tmp/file1.java:20:1",
				"/tmp/file2.java:20:2",
				"/tmp/file3.java:20:3"
		);
		// TODO
		// assertPathDetection(
		// 		"Comma or semicolon separated paths: src/test/resources/file1.java,src/test/resources/file1.py;src/test/resources/testfile",
		// 		"src/test/resources/file1.java",
		// 		"src/test/resources/file1.py",
		// 		"src/test/resources/testfile"
		// );
		assertPathDetection(
				"Comma or semicolon separated paths: src/test/resources/file1.java:20:1,src/test/resources/file1.java:20:2;src/test/resources/file1.java:20:3",
				"src/test/resources/file1.java:20:1",
				"src/test/resources/file1.java:20:2",
				"src/test/resources/file1.java:20:3"
		);
	}

	@Test
	public void testPathSurroundedBy() {
		for (final String pair : new String[]{"()", "[]"}) {
			final String start = String.valueOf(pair.charAt(0));
			final String end = String.valueOf(pair.charAt(1));

			assertPathDetection(start + "awesome.console.IntegrationTest:4" + end, "awesome.console.IntegrationTest:4", 4);
			assertPathDetection(start + "awesome.console.IntegrationTest:4:" + end, "awesome.console.IntegrationTest:4", 4);
			assertPathDetection(start + "awesome.console.IntegrationTest:4", "awesome.console.IntegrationTest:4", 4);
			assertPathDetection("awesome.console.IntegrationTest:4" + end, "awesome.console.IntegrationTest:4", 4);
			assertPathDetection(
					start + "awesome.console.IntegrationTest:4,awesome.console.IntegrationTest:5" + end,
					"awesome.console.IntegrationTest:4",
					"awesome.console.IntegrationTest:5"
			);
		}
	}

	private void assertPathDetection(final String line, final String expected, final String... more) {
		assertPathDetection(line, expected, -1, -1);
		for (String item : more) {
			assertPathDetection(line, item, -1, -1);
		}
	}

	private void assertPathDetection(final String line, final String expected, final int expectedRow) {
		assertPathDetection(line, expected, expectedRow, -1);
	}

	private void assertPathDetection(final String line, final String expected, final int expectedRow, final int expectedCol) {
		AwesomeLinkFilter filter = new AwesomeLinkFilter(getProject());

		// Test only detecting file paths - no file existence check
		List<FileLinkMatch> results = filter.detectPaths(line);
		results = results.stream().filter(i -> expected.equals(i.match)).collect(Collectors.toList());

		assertEquals("No matches in line \"" + line + "\"", 1, results.size());
		FileLinkMatch info = results.get(0);
		assertEquals(String.format("Expected filter to detect \"%s\" link in \"%s\"", expected, line), expected, info.match);

		if (expectedRow >= 0)
			assertEquals("Expected to capture row number", expectedRow, info.linkedRow);

		if (expectedCol >= 0)
			assertEquals("Expected to capture column number", expectedCol, info.linkedCol);
	}


	private void assertURLDetection(final String line, final String expected) {
		AwesomeLinkFilter filter = new AwesomeLinkFilter(getProject());

		// Test only detecting file paths - no file existence check
		List<URLLinkMatch> results = filter.detectURLs(line);

		assertEquals("No matches in line \"" + line + "\"", 1, results.size());
		URLLinkMatch info = results.get(0);
		assertEquals(String.format("Expected filter to detect \"%s\" link in \"%s\"", expected, line), expected, info.match);
	}
}
