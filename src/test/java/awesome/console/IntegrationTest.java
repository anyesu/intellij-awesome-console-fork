package awesome.console;

public class IntegrationTest {
	public static void main(final String[] args) {
		System.out.println(AwesomeLinkFilter.FILE_PATTERN);
		System.out.println(AwesomeLinkFilter.URL_PATTERN);
		System.out.println("Just a file: testfile ");
		System.out.println("Just a file: .gitignore ");
		System.out.println("Just a file: file1.java");
		System.out.println("Just a file with line num: file1.java:5");
		System.out.println("Just a file with line num and col: file1.java:5:3");
		System.out.println("Just a file with line num and col: file1.java:    5  :   3      ");
		System.out.println("Just a file with line num and col: file1.java:1606293360891972:1606293360891972");
		System.out.println("Just a file with line num and col: file_with.special-chars.js:5:3");
		System.out.println("Just a file with path: resources/file1.java");
		System.out.println("Just a file with path: src/test/resources/file1.java");
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
		System.out.println("C:\\      (by GenericFileFilter)");
		System.out.println("C:");
		System.out.println("omfg something: file://C:/Windows yay");
		System.out.println("[DEBUG] src/test/resources/file1.java:[4,4] cannot find symbol");
		System.out.println("awesome.console.AwesomeLinkFilter:5");
		System.out.println("awesome.console.AwesomeLinkFilter.java:5");
		System.out.println("something (C:\\root\\something.java) blabla");
		System.out.println("something \"C:\\root\\something.java\" blabla");
		System.out.println("something 'C:\\root\\something.java' blabla");
		System.out.println("foo https://en.wikipedia.org/wiki/Parenthesis_(disambiguation) bar");
		System.out.println("something (file1.java) blabla");
		System.out.println("(file:///tmp)");
		System.out.println("C:/Windows/Temp,");
		System.out.println("C:/Windows/Temp/test.tsx:5:3");
		System.out.println("Just a file: src/test/resources/file1.java, line 2, column 2");
		System.out.println("Just a file: src/test/resources/file1.java, line 2, coL 3");
		System.out.println("Just a file: src/test/resources/file1.java( 5 ,  4   )    ");
		System.out.println("Just a file with path: file://resources/file1.java:5:4");
		System.out.println("Just a file with path: C:\\integration\\file1.java:5:4");
		System.out.println("colon at the end: resources/file1.java:5:1:");
		System.out.println("colon at the end: C:\\integration\\file1.java:5:4:");
		System.out.println("unicode 中.txt:5 yay");
		System.out.println("regular class name [awesome.console.IntegrationTest:4]");
		System.out.println("scala class name [awesome.console.IntegrationTest$:4]");
		System.out.println("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:");

		System.out.println();
		System.out.println("Just a file with path contains \".\" and \"..\": ./src/test/resources/subdir/./file1.java");
		System.out.println("Just a file with path contains \".\" and \"..\": ./src/test/resources/subdir/../file1.java");

		System.out.println("UNC path should not be highlighted: \\\\localhost\\c$");
		System.out.println("UNC path should not be highlighted: \\\\server\\share\\folder\\myfile.txt");
		System.out.println("UNC path correctly processed by UrlFilter: file://///localhost/c$");

		System.out.println("Path with space: src/test/resources/中文 空格.txt");
		System.out.println("Path enclosed in double quotes: \"C:\\Program Files (x86)\\Windows NT\" ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/中文 空格.txt\" ");
		System.out.println("Path enclosed in double quotes: \"file://src/test/resources/中文 空格.txt\" ");
		System.out.println("Path enclosed in double quotes: \"  src/test/resources/中文 空格.txt  \" ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/中文 空格.txt\":5:4 ");
		System.out.println("Path enclosed in double quotes: \"src/test/resources/subdir/file1.java\" ");
		System.out.println("Path enclosed in double quotes: (the file name or folder name start with space or end with space)");
		System.out.println("    \"src/test/  resources/subdir/file1.java\" ");
		System.out.println("    \"src/test/resources/subdir/ file1.java\" ");
		System.out.println("    \"src/test/resources/subdir /file1.java\" ");
		System.out.println("Comma or semicolon separated paths: C:\\integration\\file1.java,C:\\integration\\file2.java;C:\\integration\\file3.java");
		System.out.println("Comma or semicolon separated paths: C:\\integration\\file1.java:20:1,C:\\integration\\file2.java:20:2;C:\\integration\\file3.java:20:3");
		System.out.println("Comma or semicolon separated paths: /tmp/file1.java,/tmp/file2.java;/tmp/file3.java");
		System.out.println("Comma or semicolon separated paths: /tmp/file1.java:20:1,/tmp/file2.java:20:2;/tmp/file3.java:20:3");
		System.out.println("Comma or semicolon separated paths(TODO): src/test/resources/file1.java,src/test/resources/file1.py;src/test/resources/testfile");
		System.out.println("Comma or semicolon separated paths: src/test/resources/file1.java:20:1,src/test/resources/file1.java:20:2;src/test/resources/file1.java:20:3");
	}
}
