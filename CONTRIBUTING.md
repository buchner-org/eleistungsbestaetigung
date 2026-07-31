# Contributing to DCS

Thank you for your interest in contributing to DCS! We welcome contributions from the community.

## How to Contribute

### Reporting Bugs

If you discover a bug, please create an issue on GitHub with:
- A clear, descriptive title
- Description of the bug and expected behavior
- Steps to reproduce the issue
- Any relevant code examples or stack traces
- Your environment (Java version, OS, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement request, please include:
- A clear, descriptive title
- Detailed description of the proposed enhancement
- Use cases and examples demonstrating the enhancement
- Possible implementation approach (if you have ideas)

### Code Contributions

1. **Fork the repository** and create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the code style and conventions used in the project

3. **Build and test** your changes:
   ```bash
   ./mvnw clean package
   ```

4. **Commit your changes** with clear, descriptive commit messages:
   ```bash
   git commit -m "Add feature: brief description of changes"
   ```

5. **Push to your fork** and submit a Pull Request to the main repository

6. **Describe your PR** with:
   - What problem does it solve or what feature does it add?
   - How have you tested the changes?
   - Any breaking changes?
   - Related issue numbers (if applicable)

## Code Style

- Follow Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use meaningful names for variables, methods, and classes
- Keep code readable and maintainable
- Add JavaDoc comments for public API elements
- Format code consistently with the existing codebase

## Development Setup

1. **Prerequisites:**
   - Java 25 or higher
   - Maven 3.8+
   - Git

2. **Clone and build:**
   ```bash
   git clone https://github.com/buchner-org/eleistungsbestaetigung.git
   cd dcs-app-api
   ./mvnw clean install
   ```

## Pull Request Process

1. Ensure your code follows the project's style guidelines
2. Update documentation if you're changing functionality
3. Add appropriate commit messages that explain the "why" behind changes
4. Ensure all builds pass without warnings
5. Request review from maintainers
6. Address feedback and push updates to your branch

## License

By contributing to this project, you agree that your contributions will be licensed under its Apache License 2.0.

## Questions?

Feel free to open an issue with your question or reach out to the development team.

Thank you for contributing!
