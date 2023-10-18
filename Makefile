FILES=file1.cs file1.py file1.java file_with.special-chars.js subdir/file1.java 中.txt

test-files:
	rm -rf src/test/resources/*
	mkdir -p src/test/resources{,/subdir}
	for i in {100001..100051}; do echo "//$$i" >> src/test/resources/testfile; done
	for F in $(FILES); do cp src/test/resources/{testfile,$$F}; done
	cp src/test/resources/{testfile,"中文 空格.txt"}
ifeq ($(OS), Windows_NT)
	cmd /c 'cd src\test\resources && mklink /J symlink subdir'
else
	cd src/test/resources && ln -s subdir symlink
endif
