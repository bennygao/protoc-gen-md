@ECHO OFF

setlocal EnableDelayedExpansion
SET BINDIR=%~dp0
SET OUTDIR=%1
SHIFT
SET PROTOC_GEN_MD_ARGS=%outdir%
protoc --plugin=protoc-gen-md=%BINDIR%\protoc-gen-md.bat --md_out=%outdir% %1 %2 %3 %4 %5 %6 %7 %8 %9
