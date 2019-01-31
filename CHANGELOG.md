## Change log
### 6.10.10 (1/16/2019 - 1/31/2019)
* mongo: support enum as map key to be consistent with other subsystem
* json: JSON.fromJSON/toJSON added validation for app beans
* search: update ES to 6.6.0

### 6.10.9 (1/7/2019 - 1/16/2019)
* cookies: support SameSite attribute, and make SessionId cookie SameSite to prevent CSRF
* bean: support enum as Map key (mongo document is not supported yet) 

### 6.10.8 (1/2/2019 - 1/4/2019)
* executor: use "TASK_REJECTED" error code when rejecting task during shutdown
* session: always try to save session even if on exception flow, in case of invalidate session or pass generated session id
