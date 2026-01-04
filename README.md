# Script Runner

A desktop application for writing and executing Kotlin scripts with live output streaming. Built with Compose Desktop.

![Screenshot placeholder](screenshots/main-window.png)

## Prerequisites

- JDK 17 or higher
- Kotlin compiler (`kotlinc`) in PATH, or set `KOTLINC_PATH` environment variable

## Build

```bash
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

## Run

```bash
./gradlew run
```

On Windows:
```bash
gradlew.bat run
```

## Features

- Code editor with Kotlin syntax highlighting (50+ keywords, built-in types)
- Live output streaming during script execution
- Script cancellation for long-running scripts
- Error parsing with clickable error locations
- Bracket matching and unmatched bracket highlighting
- Dark/Light theme toggle
- Current line highlighting
- Incremental tokenization for performance

## Architecture

```
com.scriptrunner/
├── model/          # Data classes (Script, ExecutionState, etc.)
├── executor/       # Script execution (ScriptExecutor interface)
├── parser/         # Error parsing (ErrorParser interface)
├── lexer/          # JFlex-based tokenizer, bracket matching
├── highlighting/   # Syntax highlighter (SyntaxHighlighter interface)
├── viewmodel/      # MVVM state management
└── ui/             # Compose UI components
```

Key design decisions:
- Interfaces for extensibility (`ScriptExecutor`, `SyntaxHighlighter`, `ErrorParser`)
- JFlex lexer for proper tokenization (handles multiline strings, comments)
- Incremental tokenization with line-based caching
- Flow-based output streaming

## Limitations

- Requires `kotlinc` to be installed separately
- No file save/load (scripts are in-memory only)
- Single script execution at a time
