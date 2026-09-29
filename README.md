## Project setup

### Prerequisites
- Java 17 (JDK)
- Maven 3.6+
- GitLab account with an application configured and personal access token (PAT) with `api` scope. The user is expected to have admin rights in the test project.
It is possible to use test project parameters provided in existing configuration, and required PAT token will be provided separately. 
But it is not publicly available so it will not be possible to see created issues and troubleshoot problems if needed. It is recommended to configure and use your own test project.

### Configuration
- Set PROJECT_ID, PROJECT_PATH, USER_NAME and USER_ID `src/test/resources/config.properties`
- Create file `src/test/resources/gitlab.properties` based on provided sample and store your GitLab personal access token (PAT) in it. 
<span style="color:#AA2222">**The file is ignored by git and should never be committed to the repository.**</span>
- For CI runs, create environment variable `GITLAB_ACCESS_TOKEN` with personal access token (Settings-> Secrets and variables-> Actions-> New repository secret).

### Test run   
The simplest command to run the tests with Maven wrapper (all commands provided for Windows environment):
```powershell
./mvnw clean test; ./mvnw allure:report
```
This should run the tests and generate an Allure report. See more information about test scopes and runs below.


## Limitations and scope:

1. Following API's are covered:
   * https://docs.gitlab.com/api/issues/#list-all-project-issues
   * https://docs.gitlab.com/api/issues/#retrieve-a-project-issue
   * https://docs.gitlab.com/api/issues/#create-an-issue
   * https://docs.gitlab.com/api/issues/#update-an-issue
   * https://docs.gitlab.com/api/issues/#delete-an-issue
2. Only Free plan is covered — no Premium or Ultimate logic or related fields in DTO's are covered.
3. A single user with admin rights is used for testing. The user's ID and related parameters are provided through configuration and is used where a valid user ID is required, for example when assigning an issue. 
Restrictions and differences related to non-admin user permissions are not covered in this assignment, as well as scenarios which required multiple users.
4. Fields that require additional project data are not covered in the create/update tests. This includes `milestoneId`, `milestone`, `merge_request_to_resolve_discussions_of`, and `discussion_to_resolve`, 
as testing these fields properly would require creating and maintaining additional milestones or merge requests in the test project. The same applies for some specific filtering parameters like my_reaction_emoji.
5. While tests could be run on a project with existing issues, some tests require a clean environment (see setup below). And in general it is recommended to use a dedicated test project for running the tests.
6. Simple CI workflow is provided to run the tests on GitHub Actions. PAT token is required to be provided as a secret variable in the repository settings. The workflow can be triggered manually, on commit or by schedule, and it will generate test reports as an artifact.
7. Some findings were discovered during the tests - see FINDINGS.md. Tests which are failing due to findings are disabled, you can easily find them by searching the project for the string 'FINDING-'.

## Running tests and viewing the reports

### Sample run commands

Run tests with default parameters and scope (By default, tests in the UnitTest scope are executed; tests marked WIP are excluded. Scope is defined by the `@Tag` annotation on the test class):
```powershell
./mvnw clean test; ./mvnw allure:report
```

Run tests for specific scope (specific Tag):
```powershell
./mvnw clean test -Dscope=EmptyEnvironmentTests; ./mvnw allure:report
```

Run specific test by method name (useful for debugging):
```powershell
 ./mvnw clean test -Dtest=*#listIssuesWithInvalidStringProjectIdTest ; ./mvnw allure:report
```

Run specific tests by class name (useful for debugging):
```powershell
./mvnw clean test -Dtest=*ListIssuesPaginationTest ; ./mvnw allure:report
```
Run tests with specific scope and given number of threads:
```powershell
./mvnw clean test -Dscope=EmptyEnvironmentTests -Dthreads=1; ./mvnw allure:report
```

GitHub Actions supports manual execution with:
- scope: UnitTest / EmptyEnvironmentTests / Reset / Cleanup
- threads: number of parallel test threads

Pushes and scheduled executions use:
- scope=UnitTest
- threads=2

### Viewing reports

For local runs you can see surefire reports at target/surefire-reports.
Allure reports could be found at target/site/allure-maven-plugin. To see it from IDE just use 'open in browser' option on index.html file from this directory (works for Idea, alternatively you can use one of the methods described below for CI).

For CI runs, the reports are generated as an artifact and could be downloaded from the workflow run page.
To see standalone allure report, you can use one of the following methods:
- Start webserver. I.e. with python:
```bash
cd .\allure-report\allure-maven-plugin
python -m http.server 8080
```
View report at http://localhost:8080/
- Or generate allure report with allure cli (requires installation: https://allurereport.org/docs/v2/install-for-windows/):
```bash
allure serve
```

## Test Execution Tags

Tests marked with `@Tag("WIP")` annotation are excluded from the run.

Tests are grouped by their expected environment state and execution mode.

| Tag | Intended usage | Environment |
|---|---|---|
| `UnitTest` | Normal API tests. Tests should be independent where possible and may run in parallel. | May contain test data |
| `Reset` | Manually resets the test project by deleting **all issues**. Use when a completely clean environment is required. | Must contain data; all data is removed |
| `Cleanup` | Manually removes issues created by the test suite using the `[API-TEST]` test-data prefix. Useful for cleaning up after interrupted or failed test runs. | May contain test data |
| `EmptyEnvironmentTests` | Tests API behaviour when the project contains no issues. Run manually after `Reset`. | Must be empty |

### Recommended usage

#### For normal test execution:

```powershell
./mvnw clean test; ./mvnw allure:report
```

The environment may contain existing data, but tests should avoid modifying it or data belonging to other tests.

#### For running the tests that require no existing data, you need to reset the project to an empty state. This is manual step:

Clean the environment <span style="color:#AA2222">**WARNING: This will delete all issues in the test project**</span>
```powershell
./mvnw clean test -Dscope=Reset; ./mvnw allure:report
```
then run the tests

```powershell
./mvnw clean test -Dscope=EmptyEnvironmentTests -Dthreads=1; ./mvnw allure:report
```

`Reset` is destructive and should only be used against the dedicated test project.

`Cleanup` is intended as a recovery/maintenance operation after test development or failed runs. It removes only issues identified as test data by the `[API-TEST]` prefix.

`EmptyEnvironmentTests` should be executed separately from tests that create, update, or delete issues. When isolation from other test classes is required, run them with a single thread:
