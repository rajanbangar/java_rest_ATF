#!/usr/bin/env python3
import pathlib
import os

# Create all directories first
dirs = [
    "src/main/java/com/automation/api/client",
    "src/main/java/com/automation/api/endpoints",
    "src/main/java/com/automation/api/models",
    "src/main/java/com/automation/api/specs",
    "src/main/java/com/automation/config",
    "src/main/java/com/automation/utils",
    "src/main/java/com/automation/listeners",
    "src/main/java/com/automation/reports",
    "src/test/java/com/automation/tests/users",
    "src/test/java/com/automation/tests/auth",
    "src/test/java/com/automation/tests/resources",
    "src/test/java/com/automation/data",
    "src/test/java/com/automation/base",
    "src/main/resources",
    "src/test/resources",
]

for d in dirs:
    pathlib.Path(d).mkdir(parents=True, exist_ok=True)

print("Directories created")
