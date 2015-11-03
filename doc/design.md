# Rationality
Why do we reinvent the wheel rather than only relying on open source lib?
Because with our team structure and skill set, it costs very high to use 3rd party lib in long term and large scaled project.

3rd party lib is out of our control, over years, many of them died, or changed significantly, it's not trivial to keep up.

Usually popular 3rd party lib needs to cover many different requirements and become bloat, what we need is small portion,
but we have to take all complexity, sometimes the integration cost is higher than the actual implementation.
It becomes a problem when you have to dig into their implementation deeply to troubleshoot.

Many of unpopular 3rd party lib we used is actually maintained by single person, and the quality and support varies which can be problematic.

For intrusive lib, like spring or hibernate, it impacts your design and architecture, any customization can be either impossible or convoluted to workaround.

For utility lib, it costs less to use and provides bigger value, but if we let its API spreads to many places,
it's hard to replace/update, and sometimes it's hard to let developers know which method to use, once new lib is imported, the usage is out of control.

As for nature of our project, we have good control in infrastructure (AWS) and technology stacks,
so we are able to solve the problem in the right place and use proper tools for each layers.
We would like to apply best practises of modern software development,
given that I think it is right choice to invest on our custom build framework and tech stack.

# Design principles
core-ng is designed and optimized for long term maintainability, code quality control. we prefer
* explicit over implicit
* type safe and refactory safe over dynamic type code
* deterministic over dynamic flow

the goal of core-ng framework are:

* provides consistent API, developer can just use core.framework.api.* and focus on business logic
* use plain design, not twisting how to use underlying api, (tech changes fast, this is to make it easier to catch new features and performance tuning)
* minimize the framework overhead, not to be bottleneck of performance issue, and be fast for both startup/runtime. (zero cost abstraction)
* minimize the 3rd party lib dependency, only use high quality ones.
* be cloud and operation friendly, provide enough tool for performance testing/troubleshooting
* zero config, pull all non-business related config out of app project
* embrace JAVA 8 features and syntax

# FAQ
### Why not use ORM?
From what we see, it's very hard for our developers to understand Hibernate tricks,
like session lifecycle, level1/2 cache, lazy load and etc.
also it's hard to translate the hibernate operations to actual SQL queries,
this becomes issue when performance is crucial. we want to build thinner/plain encapsulation around JDBC,

our Repository is just syntax sugar of single table operation, it's to simplify the common use cases, and be able to tune performance in low level.
also be able to control resource (db connection/transaction) precisely

### Netty vs. undertow
originally core-ng used netty to implement first prototype, then after comparing with undertow,
undertow is a more concise lib and focuses on HTTP, while Netty is general network lib,
with undertow, the impl is simpler and performance is similar, e.g simple JSON get request can easily achieve 40000+ req/seconds in our laptop.
