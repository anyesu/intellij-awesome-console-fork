FILES=file1.ts file1.cs file1.py file1.java file2.java file3.java file_with.special-chars.js subdir/file1.java 中.txt tsconfig.json

test-files:
	rm -rf src/test/resources/*
	mkdir -p src/test/resources{,/subdir}
	for i in {100001..100051}; do echo "//$$i" >> src/test/resources/testfile; done
	for F in $(FILES); do cp src/test/resources/{testfile,$$F}; done
	cp src/test/resources/{testfile,"中文 空格.txt"}
ifeq ($(OS), Windows_NT)
	-cmd /c 'rd /s /q C:\Windows\Temp\intellij-awesome-console 2>nul'
#	-cmd /c 'mklink /J C:\Windows\Temp\intellij-awesome-console src\test\resources'
	-cmd /c 'mkdir C:\Windows\Temp\intellij-awesome-console'
	-cmd /c 'copy src\test\resources C:\Windows\Temp\intellij-awesome-console'

	-cmd /c 'cd src\test\resources && mklink /J symlink subdir'
	-cmd /c 'cd src\test\resources && mklink /J invalid-symlink unknown'
else
	rm -rf /tmp/intellij-awesome-console
#	ln -sf $(shell pwd)/src/test/resources /tmp/intellij-awesome-console
	mkdir -p /tmp/intellij-awesome-console
	cp -r src/test/resources/* /tmp/intellij-awesome-console

	cd src/test/resources && ln -s subdir symlink
	cd src/test/resources && ln -s unknown invalid-symlink
endif
