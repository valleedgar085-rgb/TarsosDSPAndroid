# Contributing to TarsosDSP-Android

Thank you for your interest in contributing to TarsosDSP-Android! This document provides guidelines and instructions for contributing.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:

1. **Clear title**: Describe the issue briefly
2. **Description**: Detailed explanation of the problem
3. **Steps to reproduce**: How to recreate the issue
4. **Expected behavior**: What should happen
5. **Actual behavior**: What actually happens
6. **Environment**:
   - Device model
   - Android version
   - TarsosDSP-Android version
   - Sample rate and buffer size used
7. **Code sample**: Minimal code that reproduces the issue
8. **Logs**: Relevant logcat output

### Suggesting Features

For feature requests, please include:

1. **Use case**: Why this feature is needed
2. **Description**: Detailed explanation of the feature
3. **Examples**: How it would be used
4. **Alternatives**: Other ways to achieve the same goal

### Pull Requests

We welcome pull requests! Here's the process:

1. **Fork the repository**
2. **Create a branch**: `git checkout -b feature/your-feature-name`
3. **Make your changes**
4. **Test thoroughly**
5. **Commit**: Use clear, descriptive commit messages
6. **Push**: `git push origin feature/your-feature-name`
7. **Create Pull Request**: Describe your changes

## Development Setup

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 8 or higher
- Android SDK API 21-34
- Git

### Setup Steps

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/TarsosDSP-Android.git
cd TarsosDSP-Android

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL/TarsosDSP-Android.git

# Create a branch
git checkout -b feature/my-feature

# Build the project
./gradlew build

# Run tests
./gradlew test
```

### Project Structure

```
TarsosDSP-Android/
├── tarsosdsp-android/           # Main library
│   ├── src/main/java/          # Library source code
│   └── build.gradle            # Library build configuration
├── tarsosdsp-android-example/  # Example application
│   ├── src/main/java/          # Example source code
│   └── build.gradle            # App build configuration
├── build.gradle                # Root build configuration
├── settings.gradle             # Project settings
└── README.md                   # Documentation
```

## Code Style

### Java Style Guide

Follow Android and Google Java style guidelines:

1. **Indentation**: 4 spaces (no tabs)
2. **Line length**: Maximum 100 characters
3. **Braces**: K&R style
4. **Naming**:
   - Classes: `PascalCase`
   - Methods: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Variables: `camelCase`

### Example

```java
public class PitchDetector implements AudioProcessor {
    
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private final PitchDetectionAlgorithm algorithm;
    private float detectedPitch;
    
    public PitchDetector(PitchDetectionAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    @Override
    public boolean process(AudioEvent audioEvent) {
        // Implementation
        return true;
    }
    
    private void calculatePitch(float[] buffer) {
        // Private method implementation
    }
}
```

### Documentation

#### Javadoc Comments

All public classes, methods, and fields should have Javadoc:

```java
/**
 * Detects pitch in real-time audio streams.
 * <p>
 * This class implements the AudioProcessor interface and uses
 * various algorithms to detect the fundamental frequency.
 * </p>
 * 
 * @author Your Name
 * @see AudioProcessor
 * @see PitchDetectionAlgorithm
 */
public class PitchDetector implements AudioProcessor {
    
    /**
     * Creates a new PitchDetector with the specified algorithm.
     * 
     * @param algorithm The pitch detection algorithm to use
     * @throws IllegalArgumentException if algorithm is null
     */
    public PitchDetector(PitchDetectionAlgorithm algorithm) {
        // Implementation
    }
}
```

#### README Updates

If your contribution affects usage, update:
- README.md
- QUICKSTART.md (if applicable)
- Example code

## Testing

### Unit Tests

Write unit tests for new functionality:

```java
@Test
public void testPitchDetection() {
    PitchDetector detector = new PitchDetector(Algorithm.YIN);
    // Test implementation
    assertNotNull(detector);
}
```

### Android Tests

For Android-specific features:

```java
@RunWith(AndroidJUnit4.class)
public class AndroidAudioPlayerTest {
    
    @Test
    public void testAudioPlayback() {
        // Test implementation
    }
}
```

### Manual Testing

Test on real devices:
- At least one device with API 21-23
- At least one modern device (API 30+)
- Different manufacturers (Samsung, Google, etc.)

### Performance Testing

For audio processing changes:
- Test CPU usage
- Test memory usage
- Test battery impact
- Test with different buffer sizes
- Test with different sample rates

## Commit Guidelines

### Commit Message Format

```
type(scope): subject

body

footer
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```
feat(pitch): add support for 48kHz sample rate

Add support for 48kHz sample rate in pitch detection algorithms.
This improves accuracy for high-quality audio input.

Closes #123
```

```
fix(audio): resolve memory leak in AudioDispatcher

AudioInputStream was not being closed properly in error cases.
Added try-finally block to ensure proper cleanup.

Fixes #456
```

## Areas for Contribution

### High Priority

1. **Performance Optimization**
   - Reduce CPU usage
   - Optimize memory allocation
   - Improve battery efficiency

2. **Bug Fixes**
   - Check issue tracker
   - Fix memory leaks
   - Improve error handling

3. **Documentation**
   - Improve examples
   - Add tutorials
   - Fix typos

### Medium Priority

1. **New Features**
   - Audio file format support
   - Additional audio effects
   - New example applications

2. **Platform Support**
   - Kotlin support
   - Jetpack Compose examples
   - Kotlin coroutines integration

3. **Testing**
   - Increase test coverage
   - Add integration tests
   - Add performance benchmarks

### Low Priority

1. **Code Quality**
   - Refactoring
   - Code cleanup
   - Dependency updates

## Pull Request Checklist

Before submitting, ensure:

- [ ] Code follows style guidelines
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] No compiler warnings
- [ ] Tested on physical device
- [ ] Commit messages are clear
- [ ] PR description is detailed
- [ ] No merge conflicts

## Code Review Process

1. **Submission**: Create pull request
2. **Automated checks**: CI/CD runs tests
3. **Code review**: Maintainers review code
4. **Feedback**: Address review comments
5. **Approval**: Maintainer approves PR
6. **Merge**: PR is merged to main branch

## Questions?

- Open an issue for questions
- Check existing issues and PRs
- Read documentation thoroughly
- Review example code

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (GPL v3).

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Accept constructive criticism
- Focus on what's best for the project
- Show empathy towards others

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Personal attacks
- Publishing private information

## Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in documentation

## Thank You!

Your contributions make TarsosDSP-Android better for everyone. We appreciate your time and effort!

---

For more information, see:
- [README.md](README.md)
- [QUICKSTART.md](QUICKSTART.md)
- [CHANGELOG.md](CHANGELOG.md)
