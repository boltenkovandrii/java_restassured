## Disclaimer
Here is the list of potential problems I have found in API. Probably not all of them are actual bugs (let alone bugs worth fixing) - some could be related to configuration.
However, they're worth mentioning because they impacted the tests I developed.
This is more of a list of questions I'd pass on to the team responsible for the API.

## List of the findings

**FINDING-1** issue_type 'test_case' seems to be a valid type, accepted by API, but when it is provided in request, issue is created with default type 'issue'. 
This is not documented in the API documentation. It is unclear if this is a bug or a feature. Also looks like you can't create 'Test case' manually - probably some undocumented behavior, which requires some special configuration in the project.

**FINDING-2** It is possible to create issue with due date in the past. This is not documented in the API documentation. It is unclear if this is a bug or a feature.

**FINDING-3** According to the API documentation iid field should accept string values, but it seems to accept only integer values.

<span style="color:#AA2222">**FINDING-4**</span> Handling of invalid (negative or too big) iid's in create request is incorrect.  You receive: "message": "500 Internal Server Error". 
Even worse - issue is created and it breaks the system - you can't view issues in UI anymore and Get Issues request is also returning 500 error.
Good news is that the issue can be deleted via API and then everything works again (you will receive another 500 error, but issue will be deleted)

**FINDING-5** Looks like you can't change issue type to an incident and set severity in the same update request. Severity remains UNKNOWN for non-incident issues.

**FINDING-6** sending non-string labels in update request is accepted by GitLab API, but request is not ignored and actually updates the issue labels to an empty list. This is unexpected behavior and should be investigated further.