# Known Limitations and Issues

## Overview

This document lists all known limitations, issues, and areas for improvement in the Personal Issue Tracker application. This transparency helps users understand what to expect and helps developers prioritize future work.

## Current Status

**Version**: 1.0  
**Last Updated**: January 2026  
**Production Ready**: Yes, with noted limitations

## Known Limitations

### 1. Sprint Management

#### Single Active Sprint Per User
**Limitation**: Only one sprint can be active at a time per user.

**Impact**: Users cannot run parallel sprints for different projects.

**Workaround**: Plan sprints sequentially or use separate user accounts for different teams.

**Priority**: Medium  
**Planned Fix**: Version 2.0

#### Sprint Date Validation
**Limitation**: Sprint dates can be set in the past.

**Impact**: Users can create sprints with historical dates, which may cause confusion.

**Workaround**: Manually verify dates before creating sprints.

**Priority**: Low  
**Planned Fix**: Version 1.1

### 2. Real-time Collaboration

#### No WebSocket Support
**Limitation**: Changes made by other users are not reflected in real-time.

**Impact**: Users must refresh the page to see updates from other users.

**Workaround**: Refresh the page regularly (F5 or Ctrl/Cmd + R).

**Priority**: High  
**Planned Fix**: Version 2.0

#### No Concurrent Editing Detection
**Limitation**: System doesn't detect when multiple users edit the same issue.

**Impact**: Last save wins, potentially overwriting other users' changes.

**Workaround**: Communicate with team members before editing shared issues.

**Priority**: Medium  
**Planned Fix**: Version 2.0

### 3. Data Export and Import

#### No Built-in Export Functionality
**Limitation**: Users cannot export their data through the UI.

**Impact**: Data migration and backup require administrator access.

**Workaround**: Contact system administrator for database exports.

**Priority**: Medium  
**Planned Fix**: Version 1.2

#### No Import Functionality
**Limitation**: Cannot import data from other systems (Jira, Trello, etc.).

**Impact**: Manual data entry required when migrating from other tools.

**Workaround**: Use API endpoints to programmatically create issues.

**Priority**: Low  
**Planned Fix**: Version 2.0

### 4. Notifications

#### No Email Notifications
**Limitation**: System doesn't send email notifications for any events.

**Impact**: Users must actively check the application for updates.

**Workaround**: Set up external monitoring or check the application regularly.

**Priority**: High  
**Planned Fix**: Version 1.2

#### No In-App Notifications
**Limitation**: No notification center or bell icon for in-app notifications.

**Impact**: Users may miss important updates.

**Workaround**: Check dashboard regularly for recent activity.

**Priority**: Medium  
**Planned Fix**: Version 1.3

### 5. File Attachments

#### No File Upload Support
**Limitation**: Cannot attach files, images, or documents to issues.

**Impact**: Users must use external file storage and link in descriptions.

**Workaround**: Upload files to cloud storage (Google Drive, Dropbox) and paste links in issue descriptions.

**Priority**: High  
**Planned Fix**: Version 1.2

#### No Image Preview
**Limitation**: Cannot preview images inline in issue descriptions.

**Impact**: Users must click links to view images.

**Workaround**: Use markdown image syntax with external URLs (coming soon).

**Priority**: Low  
**Planned Fix**: Version 1.3

### 6. Search and Filtering

#### Basic Search Only
**Limitation**: Search only works on issue titles and descriptions, not comments or labels.

**Impact**: May miss relevant issues if search terms are only in comments.

**Workaround**: Use label filtering and manual review.

**Priority**: Medium  
**Planned Fix**: Version 1.2

#### No Advanced Filters
**Limitation**: Cannot combine multiple filters (e.g., "High priority AND in Sprint 1 AND has label 'bug'").

**Impact**: Users must manually filter results.

**Workaround**: Use multiple filtering steps or export data for analysis.

**Priority**: Medium  
**Planned Fix**: Version 1.3

#### No Saved Searches
**Limitation**: Cannot save frequently used search queries.

**Impact**: Users must re-enter search criteria each time.

**Workaround**: Bookmark URLs with query parameters (if supported).

**Priority**: Low  
**Planned Fix**: Version 2.0

### 7. Customization

#### No Custom Fields
**Limitation**: Cannot add custom fields to issues beyond the standard fields.

**Impact**: Users must use description or comments for additional information.

**Workaround**: Use structured text in descriptions (e.g., "Environment: Production").

**Priority**: Medium  
**Planned Fix**: Version 2.0

#### No Custom Workflows
**Limitation**: Issue workflow is fixed and cannot be customized.

**Impact**: May not match all team processes.

**Workaround**: Adapt team process to match the fixed workflow.

**Priority**: Low  
**Planned Fix**: Version 2.0

#### No Custom Issue Types
**Limitation**: Issue types are fixed (Bug, Story, Task, Epic).

**Impact**: Cannot create project-specific issue types.

**Workaround**: Use labels to categorize issues further.

**Priority**: Low  
**Planned Fix**: Version 2.0

### 8. Reporting and Analytics

#### Limited Dashboard Metrics
**Limitation**: Dashboard shows basic metrics only (counts, percentages).

**Impact**: No advanced analytics like velocity charts, burndown charts, or cumulative flow diagrams.

**Workaround**: Export data and analyze in external tools (Excel, Tableau).

**Priority**: Medium  
**Planned Fix**: Version 1.3

#### No Time Tracking
**Limitation**: No built-in time tracking functionality.

**Impact**: Cannot track time spent on issues.

**Workaround**: Use comments to log time or use external time tracking tools.

**Priority**: Medium  
**Planned Fix**: Version 2.0

#### No Velocity Tracking
**Limitation**: System doesn't calculate or display team velocity.

**Impact**: Sprint planning relies on manual estimation.

**Workaround**: Manually track completed story points per sprint.

**Priority**: Low  
**Planned Fix**: Version 1.3

### 9. Mobile Experience

#### No Native Mobile App
**Limitation**: No iOS or Android native applications.

**Impact**: Mobile experience is limited to responsive web interface.

**Workaround**: Use mobile web browser (responsive design is available).

**Priority**: Low  
**Planned Fix**: Version 3.0

#### Limited Mobile Functionality
**Limitation**: Some features are harder to use on mobile (drag-and-drop, complex forms).

**Impact**: Mobile users may have reduced productivity.

**Workaround**: Use desktop for complex tasks, mobile for viewing and simple updates.

**Priority**: Medium  
**Planned Fix**: Version 2.0

### 10. Integration and API

#### No Webhooks
**Limitation**: System doesn't support webhooks for external integrations.

**Impact**: Cannot trigger external systems when events occur.

**Workaround**: Poll API endpoints regularly or use database triggers.

**Priority**: Medium  
**Planned Fix**: Version 1.3

#### No OAuth Support
**Limitation**: Only supports email/password authentication.

**Impact**: Cannot use single sign-on (SSO) with Google, GitHub, etc.

**Workaround**: Create separate account with email/password.

**Priority**: Low  
**Planned Fix**: Version 2.0

#### Limited API Documentation
**Limitation**: API documentation is basic and may be incomplete.

**Impact**: Developers may struggle to integrate with the system.

**Workaround**: Review source code or contact support for API details.

**Priority**: Medium  
**Planned Fix**: Version 1.1

### 11. Performance

#### No Pagination on Some Views
**Limitation**: Some views load all data at once (e.g., all issues in a project).

**Impact**: Performance degrades with large datasets (>1000 issues).

**Workaround**: Use filters to reduce dataset size or split into multiple projects.

**Priority**: Medium  
**Planned Fix**: Version 1.2

#### No Lazy Loading
**Limitation**: All data is loaded upfront, not on-demand.

**Impact**: Initial page load may be slow with large datasets.

**Workaround**: Use filters to reduce initial data load.

**Priority**: Low  
**Planned Fix**: Version 1.3

### 12. User Management

#### No User Roles
**Limitation**: All users have the same permissions (no admin, viewer, editor roles).

**Impact**: Cannot restrict certain actions to specific users.

**Workaround**: Use separate accounts for different permission levels.

**Priority**: Low  
**Planned Fix**: Version 2.0

#### No Team Management
**Limitation**: No concept of teams or shared workspaces.

**Impact**: Each user has completely isolated data.

**Workaround**: Share credentials (not recommended) or use external collaboration tools.

**Priority**: High  
**Planned Fix**: Version 2.0

#### No User Profile Customization
**Limitation**: Users cannot customize their profile (avatar, bio, preferences).

**Impact**: Limited personalization.

**Workaround**: None available.

**Priority**: Low  
**Planned Fix**: Version 2.0

## Known Issues

### 1. End-to-End Tests Disabled

**Issue**: End-to-end integration tests are currently disabled.

**Impact**: Some edge cases may not be caught by automated testing.

**Status**: Tests exist but need API endpoint adjustments to match current implementation.

**Workaround**: Manual testing before production deployment.

**Priority**: Medium  
**Planned Fix**: Version 1.1

**Details**: See `docs/testing/END_TO_END_TESTS_STATUS.md` for more information.

### 2. Testcontainers Compatibility

**Issue**: Testcontainers tests may fail on some macOS configurations with Docker Desktop.

**Impact**: Developers may need to use H2 tests instead of PostgreSQL tests.

**Status**: Dual testing strategy implemented (H2 for development, Testcontainers for CI/CD).

**Workaround**: Use H2 tests for local development, Testcontainers in CI/CD pipeline.

**Priority**: Low  
**Planned Fix**: No fix planned (workaround is sufficient)

**Details**: See `docs/testing/TESTING_STRATEGY.md` for more information.

### 3. Flyway Migration Validation

**Issue**: Flyway may fail if migration files are modified after being applied.

**Impact**: Database migrations may fail, preventing application startup.

**Status**: Scripts and documentation provided for resolution.

**Workaround**: Use `./scripts/reset-dev-database.sh` to reset development database.

**Priority**: Low  
**Planned Fix**: No fix needed (expected behavior)

**Details**: See `docs/FLYWAY_TROUBLESHOOTING.md` for more information.

### 4. JWT Token Expiration

**Issue**: JWT tokens expire after 24 hours, requiring re-login.

**Impact**: Users must log in again after 24 hours.

**Status**: Working as designed, but may be inconvenient for some users.

**Workaround**: Implement "Remember Me" functionality (planned for v1.2).

**Priority**: Low  
**Planned Fix**: Version 1.2

### 5. CORS Configuration

**Issue**: CORS configuration may need adjustment for production domains.

**Impact**: Frontend may not be able to communicate with backend if domains don't match.

**Status**: Configuration is environment-specific.

**Workaround**: Update `CORS_ALLOWED_ORIGINS` environment variable.

**Priority**: High (for production deployment)  
**Planned Fix**: Documentation update in Version 1.1

## Browser Compatibility Issues

### Internet Explorer
**Status**: Not supported  
**Reason**: IE is deprecated and doesn't support modern JavaScript features  
**Workaround**: Use a modern browser (Chrome, Firefox, Safari, Edge)

### Safari < 14
**Status**: Limited support  
**Reason**: Missing some ES6+ features  
**Workaround**: Update to Safari 14 or later

### Mobile Browsers
**Status**: Partial support  
**Reason**: Some features (drag-and-drop) don't work well on touch devices  
**Workaround**: Use desktop browser for full functionality

## Performance Considerations

### Large Datasets

**Threshold**: Performance may degrade with >10,000 issues per user.

**Symptoms**:
- Slow page loads
- Laggy UI interactions
- High memory usage

**Recommendations**:
- Archive completed projects
- Use filters to reduce displayed data
- Consider splitting into multiple user accounts

### Concurrent Users

**Threshold**: System is tested with up to 100 concurrent users.

**Symptoms** (if exceeded):
- Slow response times
- Database connection pool exhaustion
- Increased error rates

**Recommendations**:
- Scale horizontally (add more backend instances)
- Increase database connection pool size
- Implement caching

## Security Considerations

### Password Reset

**Limitation**: No self-service password reset functionality.

**Impact**: Users must contact administrator to reset passwords.

**Workaround**: Administrator can reset passwords via database.

**Priority**: High  
**Planned Fix**: Version 1.2

### Two-Factor Authentication

**Limitation**: No 2FA support.

**Impact**: Accounts are protected by password only.

**Workaround**: Use strong passwords and change them regularly.

**Priority**: Medium  
**Planned Fix**: Version 2.0

### Session Management

**Limitation**: No ability to view or revoke active sessions.

**Impact**: Users cannot see where they're logged in or force logout.

**Workaround**: Change password to invalidate all sessions.

**Priority**: Low  
**Planned Fix**: Version 2.0

## Deployment Considerations

### Docker Required

**Limitation**: Application is designed to run in Docker containers.

**Impact**: Cannot easily deploy without Docker.

**Workaround**: Install Docker or manually configure services.

**Priority**: Low  
**Planned Fix**: No fix planned (Docker is the recommended deployment method)

### No Kubernetes Support

**Limitation**: No Kubernetes manifests or Helm charts provided.

**Impact**: Manual configuration required for Kubernetes deployment.

**Workaround**: Create custom Kubernetes manifests based on Docker Compose configuration.

**Priority**: Low  
**Planned Fix**: Version 2.0

### No Auto-Scaling

**Limitation**: No built-in auto-scaling configuration.

**Impact**: Manual scaling required based on load.

**Workaround**: Monitor metrics and scale manually.

**Priority**: Low  
**Planned Fix**: Version 2.0

## Documentation Gaps

### API Documentation

**Status**: Basic API documentation available  
**Gap**: No interactive API documentation (Swagger UI)  
**Priority**: Medium  
**Planned Fix**: Version 1.1

### Architecture Documentation

**Status**: Basic architecture documented  
**Gap**: No detailed component diagrams or sequence diagrams  
**Priority**: Low  
**Planned Fix**: Version 1.2

### Deployment Examples

**Status**: Docker Compose examples provided  
**Gap**: No examples for cloud providers (AWS, Azure, GCP)  
**Priority**: Low  
**Planned Fix**: Version 2.0

## Roadmap

### Version 1.1 (Q2 2026)
- Fix end-to-end tests
- Improve API documentation
- Add sprint date validation
- Update CORS documentation

### Version 1.2 (Q3 2026)
- Email notifications
- File attachment support
- Data export functionality
- Password reset functionality
- Pagination improvements
- Advanced search

### Version 1.3 (Q4 2026)
- In-app notifications
- Webhook support
- Advanced reporting (velocity, burndown charts)
- Saved searches

### Version 2.0 (2027)
- Real-time collaboration (WebSocket)
- Team management and shared workspaces
- Custom fields and workflows
- OAuth/SSO support
- User roles and permissions
- Time tracking
- Mobile app (iOS/Android)

## Contributing

If you encounter issues not listed here, please:

1. Check existing documentation
2. Search for similar issues
3. Report new issues with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (browser, OS, version)
   - Screenshots if applicable

## Support

For questions or issues:

- **Documentation**: Check the docs folder
- **Known Issues**: Review this document
- **Bug Reports**: Use the issue tracker
- **Feature Requests**: Contact the development team
- **Security Issues**: Contact security@example.com

---

**Note**: This document is updated regularly as new limitations are discovered and existing ones are resolved. Last review: January 2026.

