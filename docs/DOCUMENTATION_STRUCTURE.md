# Documentation Structure and Naming Conventions

[Versión en Español](DOCUMENTATION_STRUCTURE.es.md)

## Overview

This document describes the documentation structure and naming conventions used throughout the Personal Issue Tracker project.

## Naming Conventions

### Language Versions

All documentation files follow a consistent naming pattern:

- **English (Primary)**: `filename.md` or `filename.en.md`
- **Spanish**: `filename.es.md`

### Examples

```
README.md           # English version (primary)
README.en.md        # English version (explicit)
README.es.md        # Spanish version

SECURITY.md         # English version (primary)
SECURITY.es.md      # Spanish version
```

## Directory Structure

```
docs/
├── README.md                           # Documentation index (English)
├── README.es.md                        # Documentation index (Spanish)
├── INDEX.md                            # Complete documentation index
├── DEVELOPMENT.md                      # Development guide
├── SECURITY.md                         # Security guidelines
├── PRODUCTION_DEPLOYMENT.md            # Production deployment guide
├── DOCUMENTATION_STRUCTURE.md          # This file
├── DOCUMENTATION_STRUCTURE.es.md       # This file (Spanish)
│
├── architecture/                       # System architecture
│   ├── README.md                       # Architecture overview (English)
│   └── README.es.md                    # Architecture overview (Spanish)
│
├── development/                        # Development guides
│   ├── FRONTEND_BACKEND_INTEGRATION_ANALYSIS.md
│   ├── FRONTEND_ERRORS_FIXED.md
│   ├── gitignore-guide.md
│   ├── gitignore-summary.md
│   ├── INTEGRATION_SUMMARY.md
│   └── internationalization-summary.md
│
├── fixes/                              # Bug fixes documentation
│   ├── modal_layout_fixes.md
│   └── sprint_completion_fixes.md
│
├── improvements/                       # Feature improvements
│   ├── error_handling_improvements.md
│   ├── project_notifications_implementation.md
│   ├── project_notifications_implementation.es.md
│   ├── sprint_activation_modal_improvements.md
│   ├── sprint_activation_validation.md
│   ├── sprint_completion_backlog_logic.md
│   ├── sprint_notifications_system.md
│   ├── sprint_notifications_system.es.md
│   ├── test_login_improvements.md
│   └── tooltip_ui_improvement.md
│
├── security/                           # Security documentation
│   ├── security-guidelines.md
│   ├── SECURITY.md                     # Security guide (English)
│   └── SECURITY.es.md                  # Security guide (Spanish)
│
└── testing/                            # Testing documentation
    ├── README_TESTING.md
    ├── test_sprint_activation.sh
    ├── test_sprint_calendar.sh
    ├── TESTCONTAINERS_TROUBLESHOOTING.md
    └── TESTING_STRATEGY.md
```

## Documentation Categories

### 1. Architecture (`architecture/`)
- System design and architecture diagrams
- Design patterns and architectural decisions
- Component relationships and data flow

### 2. Development (`development/`)
- Development setup and configuration
- Integration guides
- Frontend-backend integration
- Internationalization guides

### 3. Fixes (`fixes/`)
- Bug fixes and issue resolutions
- Problem descriptions and solutions
- Troubleshooting guides

### 4. Improvements (`improvements/`)
- Feature enhancements
- UI/UX improvements
- Performance optimizations
- New functionality documentation

### 5. Security (`security/`)
- Security best practices
- Authentication and authorization
- Data protection guidelines
- Security audit results

### 6. Testing (`testing/`)
- Testing strategies and approaches
- Test scripts and automation
- Testing troubleshooting
- Test coverage reports

## File Organization Rules

### 1. Location
- Root-level documentation (README, main guides) → Project root or `docs/`
- Category-specific documentation → Appropriate subdirectory in `docs/`
- Module-specific documentation → Module directory (e.g., `backend/`, `frontend/`)

### 2. Naming
- Use descriptive, lowercase names with underscores or hyphens
- Include language suffix for non-English versions (`.es.md`)
- Use consistent naming across related files

### 3. Content
- Always provide both English and Spanish versions for important documents
- Link between language versions at the top of each file
- Keep documentation up-to-date with code changes

## Recent Reorganization (January 2026)

### Files Moved

1. **NOTIFICACIONES_IMPLEMENTADAS.md** → `docs/improvements/project_notifications_implementation.md`
   - Translated to English
   - Created Spanish version: `project_notifications_implementation.es.md`

2. **.env.prod.template** → `backend/.env.prod.template`
   - Moved to backend directory for better organization

### Files Renamed

1. Root README files:
   - `README.md` (Spanish) → `README.es.md`
   - Created new `README.md` (English, primary)
   - Created `README.en.md` (English, explicit)

2. Docs README files:
   - Updated to follow consistent naming convention
   - Added language version links

## Contributing Guidelines

When adding new documentation:

1. **Choose the right location**: Place files in the appropriate category directory
2. **Follow naming conventions**: Use consistent naming with language suffixes
3. **Provide translations**: Create both English and Spanish versions for important docs
4. **Link versions**: Add language version links at the top of each file
5. **Update indexes**: Update `INDEX.md` and category README files
6. **Keep it organized**: Don't leave documentation files in the root unless they're main guides

## Language Version Links

Add these links at the top of each documentation file:

```markdown
# Document Title

[🇪🇸 Versión en Español](filename.es.md) | [🇬🇧 English Version](filename.en.md)
```

## Maintenance

- Review documentation structure quarterly
- Remove outdated documentation
- Consolidate duplicate information
- Ensure all important docs have translations
- Update this guide when structure changes
