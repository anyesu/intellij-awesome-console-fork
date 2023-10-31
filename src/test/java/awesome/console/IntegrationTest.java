package awesome.console;

public class IntegrationTest {
	public static void main(final String[] args) {
		System.out.println(AwesomeLinkFilter.FILE_PATTERN);
		System.out.println(AwesomeLinkFilter.URL_PATTERN);
		System.out.println("Just a file: testfile ");
		System.out.println("Just a file: .gitignore ");
		System.out.println("Just a file: file1.java");
		System.out.println("Just a file with line num: file1.java:5");
		System.out.println("Just a file with line num: file1.cs:line 4");
		System.out.println("Just a file with line num and col: file1.java:5:3");
		System.out.println("Just a file with line num and col: file1.java:    5  :   10      ");
		System.out.println("Just a file with line num and col: file1.java:1606293360891972:1606293360891972");
		System.out.println("Just a file with line num and col: file_with.special-chars.js:5:3");
		System.out.println("Just a file with path: resources/file1.java");
		System.out.println("Just a file with path: src/test/resources/file1.java");
		System.out.println("Just a file with path: \\src/test/resources/file1.java");
		System.out.println("Just a file with path: /src/test/resources/file1.java");
		System.out.println("Just a file with path: ./src/test/resources/file1.java");
		System.out.println("bla-bla at (AwesomeLinkFilter.java:150) something");
		System.out.println("Absolute path: /tmp");
		System.out.println("omfg something: git://xkcd.com/ yay");
		System.out.println("omfg something: http://xkcd.com/ yay");
		System.out.println("omfg something: http://8.8.8.8/ yay");
		System.out.println("omfg something: https://xkcd.com/ yay");
		System.out.println("omfg something: http://xkcd.com yay");
		System.out.println("omfg something: ftp://8.8.8.8:2424 yay");
		System.out.println("omfg something: file:///tmp yay");
		System.out.println("omfg something: ftp://user:password@xkcd.com:1337/some/path yay");
		System.out.println("C:\\Windows\\Temp\\");
		System.out.println("C:\\Windows\\Temp");
		System.out.println("C:\\Windows/Temp");
		System.out.println("C:/Windows/Temp");
		System.out.println("C:\\\\");
		System.out.println("C:\\");
		System.out.println("C:/");
		System.out.println("\u001b[33mC:\u001b[0m");
		System.out.println("omfg something: file://C:/Windows yay");
		System.out.println("[DEBUG] src/test/resources/file1.java:[4,4] cannot find symbol");
		System.out.println("awesome.console.AwesomeLinkFilter:5");
		System.out.println("awesome.console.AwesomeLinkFilter.java:50");
		System.out.println("something (C:\\root\\something.java) blabla");
		System.out.println("something \"C:\\root\\something.java\" blabla");
		System.out.println("something 'C:\\root\\something.java' blabla");
		System.out.println("foo https://en.wikipedia.org/wiki/Parenthesis_(disambiguation) bar");
		System.out.println("something (file1.java) blabla");
		System.out.println("(file:///tmp)");
		System.out.println("C:/Windows/Temp,");
		System.out.println("C:/Windows/Temp/test.tsx:5:3");
		System.out.println("Just a file: src/test/resources/file1.java, line 2, column 2");
		System.out.println("Just a file: src/test/resources/file1.java, line 2, coL 30");
		System.out.println("Just a file: src/test/resources/file1.java( 5 ,  4   )    ");
		System.out.println("Just a file: src/test/resources/file1.java (30 KiB)");
		System.out.println("Just a file with path: file://resources/file1.java:5:40");
		System.out.println("Just a file with path: C:\\integration\\file1.java:5:4");
		System.out.println("colon at the end: resources/file1.java:50:10:");
		System.out.println("colon at the end: C:\\integration\\file1.java:5:4:");
		System.out.println("unicode 中.txt:5 yay");
		System.out.println("regular class name [awesome.console.IntegrationTest:40]");
		System.out.println("scala class name [awesome.console.IntegrationTest$:4]");
		System.out.println("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:");

		System.out.println();
		System.out.println("Just a file in user's home directory: ~/.gradle ");
		System.out.println("Just a file in user's home directory: ~\\.gradle ");

		System.out.println("Just a file with path contains \"\u001b[35m.\u001b[0m\" and \"\u001b[35m..\u001b[0m\": ./src/test/resources/subdir/./file1.java");
		System.out.println("Just a file with path contains \"\u001b[35m.\u001b[0m\" and \"\u001b[35m..\u001b[0m\": ./src/test/resources/subdir/../file1.java");

		System.out.println("\u001b[33mUNC path should not be highlighted\u001b[0m: \\\\localhost\\c$");
		System.out.println("\u001b[33mUNC path should not be highlighted\u001b[0m: \\\\server\\share\\folder\\myfile.txt");
		System.out.println("\u001b[33mUNC path correctly processed by UrlFilter\u001b[0m: file://///localhost/c$");

		System.out.println("\u001b[33mPath with space is not highlighted by default\u001b[0m: src/test/resources/中文 空格.txt");
		System.out.println("Path enclosed in double quotes: \"C:\\Program Files (x86)\\Windows NT\" ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/中文 空格.txt\" ");
		System.out.println("Path enclosed in double quotes: \"file://src/test/resources/中文 空格.txt\" ");
		System.out.println("Path enclosed in double quotes (\u001b[33mshould not be highlighted\u001b[0m): \"  src/test/resources/中文 空格.txt  \" ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/中文 空格.txt\":5:4 ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/subdir/file1.java\" ");
		System.out.println("Path enclosed in double quotes: (the file name or folder name start with space or end with space)");
		System.out.println("    \"src/test/  resources/subdir/file1.java\" ");
		System.out.println("    \"src/test/resources/subdir/ file1.java\" ");
		System.out.println("    \"src/test/resources/subdir /file1.java\" ");

		System.out.println("Comma or semicolon separated paths: C:\\integration\\file1.java,C:\\integration\\file2.java;C:\\integration\\file3.java");
		System.out.println("Comma or semicolon separated paths: C:\\integration\\file1.java:20:1,C:\\integration\\file2.java:20:5;C:\\integration\\file3.java:20:10");
		System.out.println("Comma or semicolon separated paths: /tmp/file1.java,/tmp/file2.java;/tmp/file3.java");
		System.out.println("Comma or semicolon separated paths: /tmp/file1.java:20:1,/tmp/file2.java:20:5;/tmp/file3.java:20:10");
		System.out.println("Comma or semicolon separated paths(\u001b[33mTODO split these\u001b[0m): src/test/resources/file1.java,src/test/resources/file1.py;src/test/resources/testfile");
		System.out.println("Comma or semicolon separated paths: src/test/resources/file1.java:20:1,src/test/resources/file1.java:20:5;src/test/resources/file1.java:20:10 ");

		System.out.println("Java stackTrace: at awesome.console.AwesomeLinkFilterTest.testFileWithoutDirectory(AwesomeLinkFilterTest.java:14)");

		for (final String pair : new String[]{"()", "[]", "''"}) {
			final String start = String.valueOf(pair.charAt(0));
			final String end = String.valueOf(pair.charAt(1));

			System.out.println("Path surrounded by: " + start + "awesome.console.IntegrationTest:2" + end);
			System.out.println("Path surrounded by: " + start + "awesome.console.IntegrationTest:10:" + end);
			System.out.println("Path surrounded by: " + start + "awesome.console.IntegrationTest:30");
			System.out.println("Path surrounded by: " + "awesome.console.IntegrationTest:40" + end);
			System.out.println("Path surrounded by: " + start + "awesome.console.IntegrationTest:45,awesome.console.IntegrationTest:50" + end);
		}

		System.out.println("\u001b[33mIgnore matches\u001b[0m: ./ . .. ... ./ ../ ././../. / // /// \\ \\\\ \\\\\\");

		System.out.println("Non-indexed files in the project: build/patchedPluginXmlFiles/plugin.xml is not plugin.xml");

		System.out.println("Just a symlink: src/test/resources/symlink/file1.java");
		System.out.println("Just a symlink: src/test/resources/symlink/file1.java:10:6");

		System.out.println("Illegal char: \u001b[33m\u0001file1.java\u001b[0m");
		System.out.println("Illegal char: \u001b[33m\u001ffile1.java\u001b[0m");
		System.out.println("Illegal char: \u001b[33m\u0021file1.java\u001b[0m");
		System.out.println("Illegal char: \u001b[33m\u007ffile1.java\u001b[0m");

		System.out.println("\u001b[33mUse ignore style to prevent this(from vite-plugin-compression) to be highlighted by GrCompilationErrorsFilterProvider\u001b[0m: 291.23kb / gzip: 44.09kb");

		System.out.println("╭─[C:\\integration\\file1.java:19:2]");
		System.out.println("╭─[C:\\integration\\file1.java:19]");
		System.out.println("╭─ C:\\integration\\file1.java:19:10");
		System.out.println("--> [C:\\integration\\file1.java:19:5]");
		System.out.println("--> C:\\integration\\file1.java:19:3");
		System.out.println("\u001b[31mWARNING: Illegal reflective access by com.intellij.util.ReflectionUtil (file:/C:/integration/file1.java) to field java.io.DeleteOnExitHook.files\u001b[0m");

		System.out.println("git console log: \u001b[31mwarning: LF will be replaced by CRLF in README.md.\u001b[0m");
		System.out.println("\u001b[31merror\u001b[0m \u001b[37mTS18003\u001b[0m: No inputs were found in config file 'tsconfig.json'. ");
		System.out.println(".");
		System.out.println("..");
		System.out.println("Path end with a dot: file1.java.");
		System.out.println("Path end with a dot: \"file1.java\".");
		System.out.println("Path end with a dot: src/test/resources/subdir/.");
		System.out.println("Path end with a dot: src/test/resources/subdir/..");
		System.out.println("Path end with a dot: src/test/resources/subdir...");

		System.out.println("Gradle build task failed with an exception: Build file 'build.gradle' line: 14");
	}
}
