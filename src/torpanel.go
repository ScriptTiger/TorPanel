package main

import (
	"os"
	"os/exec"
	"path/filepath"
)

func main() {

	// Locate base directory of launcher and establish path to java
	dir, err := os.Executable()
	if err != nil {os.Exit(1)}
	dir, err = filepath.EvalSymlinks(dir)
	if err != nil {os.Exit(1)}
	dir = filepath.Dir(dir)
	javaPath := filepath.Join(filepath.Join(dir, "bin"), javaName)

	// Change working directory to base directory
	os.Chdir(dir)

	// Launch app
	exec.Command(javaPath, "-m", "torpanel/torpanel.Main").Start()

	// Exit launcher
	os.Exit(0)
}