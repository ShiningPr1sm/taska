<div align="center">

# 📓 / taska
 
[![GitHub stars](https://img.shields.io/github/stars/ShiningPr1sm/taska?style=flat-square)](https://github.com/ShiningPr1sm/taska/stargazers)
[![GitHub last commit](https://img.shields.io/github/last-commit/ShiningPr1sm/taska?label=last%20update&style=flat-square)](https://github.com/ShiningPr1sm/taska/commits)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/ShiningPr1sm/taska?label=version&style=flat-square)](https://github.com/ShiningPr1sm/taska/releases)

<img width="800" height="450" alt="taska" src="https://github.com/user-attachments/assets/a79edae1-6c52-4217-b0c2-e1e40a5ab417" />

 
</div>

> A fast, keyboard-driven terminal task manager for Windows — organize your lists and tasks without ever touching a mouse.
 
---
 
## Overview
 
**taska** is a TUI (Text User Interface) task manager built for people who live in the terminal. Instead of a bloated GUI, it gives you a clean three-pane layout — **Lists**, **Tasks**, and **Details** — that you can fly through entirely with the keyboard. Every list and task is stored locally as JSON, themes and fonts are fully customizable, and hotkeys can be rebound to whatever feels natural to you.
 
Built with **Java 21**, [**Lanterna**](https://github.com/mabe02/lanterna) for the terminal UI, and **Jackson** for persistence.
 
## Key Features
 
* **Lists & Tasks**: Create, rename, edit, and delete lists and tasks on the fly. Each task tracks its status, priority, creation date, and notes.
* **Priority at a glance**: Every task shows a colored plate (🟢 low / 🟡 medium / 🔴 high) so you can scan your workload without reading a single word.
* **Fully keyboard-driven**: No mouse required — navigate, create, edit, delete, and toggle tasks entirely through hotkeys.
* **Rebindable hotkeys**: Don't like the defaults? Open the rebind menu and set your own combination for any action — it's saved automatically.
* **Themes & fonts**: Choose from several built-in color themes and any monospaced font installed on your system, applied instantly.
* **Self-updating**: taska checks GitHub for new releases on startup and offers to update itself — no manual downloads required.

## Getting Started
 
### Prerequisites
 
* JDK 21+
* Maven (for building from source)
### Download
 
* You can download a `.jar` or `.exe` file from [Releases](https://github.com/ShiningPr1sm/taska/releases).
 
## Keyboard Shortcuts
 
All hotkeys below are the defaults — press **`Ctrl+R`** inside the app at any time to rebind any of them to a key combination of your choice.
 
| Key       | Action                                               |
|-----------|------------------------------------------------------|
| `l`       | Focus the **Lists** panel                            |
| `k`       | Focus the **Tasks** panel                            |
| `Space`   | Toggle the selected task as done / not done          |
| `Ctrl+N`  | Create a new list                                    |
| `a`       | Create a new task in the current list                |
| `e`       | Edit the selected list or task (context-aware)       |
| `d`       | Delete the selected list or task (context-aware)     |
| `t`       | Open the theme picker                                |
| `f`       | Open the font picker                                 |
| `h`       | Show the full list of current hotkeys                |
| `Ctrl+R`  | Open the rebind menu to customize any hotkey         |
| `q`       | Quit taska                                           |
 
> [!TIP]
> `e` and `d` work on whichever panel is currently focused — highlight **Lists** to edit/delete a list, or **Tasks** to edit/delete a task.
 
### Acknowledgments
 
If you find a bug, error, or typo, please submit a report in the [Issues](https://github.com/ShiningPr1sm/taska/issues) section. Thank you very much for using this program!
