# Personal Issue Tracker - User Guide

## Welcome to Personal Issue Tracker

Personal Issue Tracker is a powerful, intuitive project management application inspired by Jira. This guide will help you get started and make the most of all features.

## Table of Contents

1. [Getting Started](#getting-started)
2. [User Authentication](#user-authentication)
3. [Managing Projects](#managing-projects)
4. [Working with Issues](#working-with-issues)
5. [Sprint Management](#sprint-management)
6. [Labels and Organization](#labels-and-organization)
7. [Dashboard and Reporting](#dashboard-and-reporting)
8. [Comments and Collaboration](#comments-and-collaboration)
9. [Keyboard Shortcuts](#keyboard-shortcuts)
10. [Tips and Best Practices](#tips-and-best-practices)
11. [Troubleshooting](#troubleshooting)

## Getting Started

### Accessing the Application

1. Open your web browser
2. Navigate to the application URL (e.g., `http://localhost:3000` for local development)
3. You'll see the login page

### First Time Setup

1. Click "Register" to create a new account
2. Enter your email address, name, and password
3. Click "Create Account"
4. You'll be automatically logged in and redirected to the dashboard

## User Authentication

### Logging In

1. Enter your email address
2. Enter your password
3. Click "Sign In"

**Security Note**: Your session will remain active for 24 hours. After that, you'll need to log in again.

### Logging Out

1. Click your profile icon in the top right corner
2. Select "Logout"

### Changing Your Password

Currently, password changes must be done through the system administrator. Contact support if you need to change your password.

## Managing Projects

### Creating a Project

1. Click the "+" button next to "Projects" in the sidebar
2. Fill in the project details:
   - **Name**: A descriptive name for your project (e.g., "Website Redesign")
   - **Key**: A short, unique identifier (e.g., "WEB") - used in issue keys
   - **Description**: Optional detailed description
3. Click "Create Project"

**Project Key Rules**:
- Must be unique across your account
- 2-10 characters
- Uppercase letters only
- No spaces or special characters

### Viewing Projects

- All your projects are listed in the sidebar under "Projects"
- Click on a project name to view its issues
- The project list shows the number of issues in each project

### Editing a Project

1. Navigate to the project
2. Click the "Edit" button (pencil icon)
3. Update the project details
4. Click "Save Changes"

### Deleting a Project

1. Navigate to the project
2. Click the "Delete" button (trash icon)
3. Confirm the deletion

**Warning**: Deleting a project will permanently delete all associated issues, comments, and audit history. This action cannot be undone.

## Working with Issues

### Creating an Issue

**Method 1: From Project View**
1. Navigate to a project
2. Click "Create Issue"
3. Fill in the issue details
4. Click "Create"

**Method 2: From Sprint Board**
1. Navigate to a sprint
2. Click "+" in any column
3. Fill in the issue details
4. Click "Create"

### Issue Fields

- **Title**: Brief description of the issue (required)
- **Description**: Detailed description with context
- **Type**: Bug, Story, Task, or Epic
- **Priority**: Low, Medium, High, or Critical
- **Story Points**: Estimation of effort (optional)
- **Labels**: Tags for categorization (optional)
- **Sprint**: Assign to a sprint (optional)
- **Parent Epic**: Link to a parent epic (optional)

### Issue Workflow

Issues follow a fixed workflow:

```
BACKLOG → SELECTED_FOR_DEVELOPMENT → IN_PROGRESS → IN_REVIEW → DONE
```

**Status Descriptions**:
- **BACKLOG**: New issues waiting to be prioritized
- **SELECTED_FOR_DEVELOPMENT**: Ready to be worked on
- **IN_PROGRESS**: Currently being worked on
- **IN_REVIEW**: Completed and awaiting review
- **DONE**: Completed and reviewed

### Updating Issue Status

**Method 1: From Issue Detail**
1. Open the issue
2. Click the status dropdown
3. Select the new status

**Method 2: Drag and Drop (Sprint Board)**
1. Navigate to the sprint board
2. Drag the issue card to the desired column

### Viewing Issue History

1. Open an issue
2. Scroll to the "History" section
3. View all changes with timestamps and user information

### Searching and Filtering Issues

**Global Search**:
1. Use the search bar at the top
2. Type keywords from issue title or description
3. Results appear instantly

**Filtering**:
- Filter by project using the sidebar
- Filter by sprint using the sprint selector
- Filter by status using the sprint board columns
- Filter by labels (click on a label to see all issues with that label)

## Sprint Management

### Creating a Sprint

1. Click "Sprints" in the sidebar
2. Click "Create Sprint"
3. Fill in the sprint details:
   - **Name**: Sprint name (e.g., "Sprint 1", "Q1 Sprint")
   - **Start Date**: When the sprint begins
   - **End Date**: When the sprint ends
   - **Goal**: Optional sprint objective
4. Click "Create Sprint"

### Planning a Sprint

1. Navigate to the sprint
2. Click "Add Issues"
3. Select issues from the backlog
4. Click "Add to Sprint"

**Tips**:
- Consider team capacity when adding issues
- Use story points to estimate workload
- Prioritize high-value items
- Include a mix of issue types

### Starting a Sprint

1. Navigate to the sprint
2. Click "Start Sprint"
3. Confirm the action

**Note**: Only one sprint can be active at a time. Starting a new sprint will complete the current active sprint.

### Completing a Sprint

1. Navigate to the active sprint
2. Click "Complete Sprint"
3. Review incomplete issues
4. Choose what to do with incomplete issues:
   - Move to backlog
   - Move to next sprint
5. Confirm completion

### Sprint Board (Kanban View)

The sprint board provides a visual representation of work in progress:

1. Navigate to an active sprint
2. Click "Board View"
3. View issues organized by status
4. Drag and drop to update status
5. Click on any issue to view details

## Labels and Organization

### Creating Labels

1. Click "Labels" in the sidebar
2. Click "Create Label"
3. Enter label name
4. Choose a color
5. Click "Create"

### Using Labels

Labels help organize and categorize issues across projects:

- **By Feature**: "authentication", "ui", "api"
- **By Priority**: "urgent", "nice-to-have"
- **By Type**: "bug", "enhancement", "documentation"
- **By Status**: "blocked", "needs-review"

### Applying Labels to Issues

1. Open an issue
2. Click "Add Label"
3. Select existing labels or create new ones
4. Labels appear as colored tags on the issue

### Filtering by Labels

1. Click on any label
2. View all issues with that label
3. Combine with other filters for precise results

## Dashboard and Reporting

### Dashboard Overview

The dashboard provides a quick overview of your work:

- **Active Sprint Summary**: Current sprint progress
- **Recent Issues**: Latest issues you've worked on
- **Issue Distribution**: Visual breakdown by status, type, and priority
- **Project Statistics**: Metrics for each project

### Understanding Metrics

**Sprint Progress**:
- Total issues in sprint
- Completed issues
- In-progress issues
- Completion percentage

**Issue Distribution**:
- By Status: See workflow bottlenecks
- By Type: Understand work composition
- By Priority: Track urgent items

**Project Statistics**:
- Total issues per project
- Open vs. closed issues
- Average completion time

### Exporting Data

Currently, data export is not available through the UI. Contact your system administrator for data export requests.

## Comments and Collaboration

### Adding Comments

1. Open an issue
2. Scroll to the "Comments" section
3. Type your comment
4. Click "Add Comment"

**Comment Tips**:
- Use @mentions to notify team members (coming soon)
- Include relevant context and links
- Be clear and concise
- Use markdown for formatting (coming soon)

### Editing Comments

1. Hover over your comment
2. Click the "Edit" button
3. Update the comment
4. Click "Save"

**Note**: You can only edit your own comments.

### Deleting Comments

1. Hover over your comment
2. Click the "Delete" button
3. Confirm deletion

**Note**: You can only delete your own comments.

## Keyboard Shortcuts

### Global Shortcuts

- `Ctrl/Cmd + K`: Open global search
- `Ctrl/Cmd + N`: Create new issue
- `Ctrl/Cmd + P`: Create new project
- `Ctrl/Cmd + S`: Create new sprint
- `Esc`: Close modal/dialog

### Navigation Shortcuts

- `G then D`: Go to Dashboard
- `G then P`: Go to Projects
- `G then S`: Go to Sprints
- `G then L`: Go to Labels

### Issue Shortcuts (when viewing an issue)

- `E`: Edit issue
- `C`: Add comment
- `Delete`: Delete issue (with confirmation)

## Tips and Best Practices

### Project Organization

1. **Use Clear Project Keys**: Choose memorable, descriptive keys (e.g., "WEB" for website, "API" for API project)
2. **Write Detailed Descriptions**: Help team members understand project context
3. **Archive Completed Projects**: Keep your workspace clean

### Issue Management

1. **Write Clear Titles**: Use action verbs (e.g., "Fix login bug", "Add user profile page")
2. **Provide Context**: Include steps to reproduce, expected behavior, and actual behavior
3. **Use Story Points**: Estimate effort to help with sprint planning
4. **Link Related Issues**: Use parent epics to group related work
5. **Update Status Regularly**: Keep the board current

### Sprint Planning

1. **Set Realistic Goals**: Don't overcommit
2. **Balance Work Types**: Mix bugs, features, and technical debt
3. **Review Velocity**: Learn from past sprints
4. **Daily Updates**: Keep the board current
5. **Retrospectives**: Review what worked and what didn't

### Label Strategy

1. **Be Consistent**: Use standard naming conventions
2. **Don't Over-Label**: Too many labels become noise
3. **Use Colors Wisely**: Similar colors for related labels
4. **Review Regularly**: Remove unused labels

## Troubleshooting

### I Can't Log In

**Possible Solutions**:
1. Verify your email and password are correct
2. Check if Caps Lock is on
3. Try resetting your password
4. Clear browser cache and cookies
5. Try a different browser

### Issues Not Loading

**Possible Solutions**:
1. Refresh the page (F5 or Ctrl/Cmd + R)
2. Check your internet connection
3. Clear browser cache
4. Try logging out and back in

### Can't Create Issues

**Possible Solutions**:
1. Ensure you have a project created first
2. Check that all required fields are filled
3. Verify your session hasn't expired
4. Try refreshing the page

### Sprint Board Not Updating

**Possible Solutions**:
1. Refresh the page
2. Check if you have an active sprint
3. Verify issues are assigned to the sprint
4. Clear browser cache

### Performance Issues

**Possible Solutions**:
1. Close unnecessary browser tabs
2. Clear browser cache
3. Disable browser extensions
4. Use a modern browser (Chrome, Firefox, Safari, Edge)
5. Check your internet connection speed

### Data Not Saving

**Possible Solutions**:
1. Check your internet connection
2. Verify your session hasn't expired
3. Try the operation again
4. Refresh the page and retry
5. Contact support if the issue persists

## Getting Help

### In-App Help

- Hover over any field for tooltips
- Look for "?" icons for contextual help
- Check the dashboard for quick tips

### Support Resources

- **Documentation**: Check the docs folder for technical documentation
- **System Administrator**: Contact your admin for account issues
- **Bug Reports**: Report bugs through the issue tracker itself

### Reporting Issues

When reporting a problem, include:
1. What you were trying to do
2. What you expected to happen
3. What actually happened
4. Steps to reproduce
5. Browser and operating system
6. Screenshots if applicable

## Appendix

### Issue Types Explained

- **Bug**: Something that's broken and needs fixing
- **Story**: A new feature or enhancement from user perspective
- **Task**: General work item that doesn't fit other categories
- **Epic**: Large body of work that can be broken down into smaller issues

### Priority Levels

- **Critical**: System is broken, immediate attention required
- **High**: Important issue that should be addressed soon
- **Medium**: Normal priority, address in regular workflow
- **Low**: Nice to have, address when time permits

### Status Workflow Details

The workflow is designed to ensure quality and visibility:

1. **BACKLOG**: All new issues start here
2. **SELECTED_FOR_DEVELOPMENT**: Prioritized and ready to work on
3. **IN_PROGRESS**: Active development
4. **IN_REVIEW**: Code review, testing, or approval
5. **DONE**: Completed and verified

### Data Privacy

- Your data is isolated from other users
- Only you can see your projects, issues, and sprints
- Audit logs track all changes for accountability
- Data is encrypted at rest and in transit

### Browser Compatibility

Supported browsers:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

For best experience, use the latest version of your preferred browser.

---

**Version**: 1.0  
**Last Updated**: January 2026  
**Need Help?**: Contact your system administrator

