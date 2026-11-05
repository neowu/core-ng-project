# Rationality

Why do we reinvent the wheel rather than only relying on open source lib?
Because with our team structure and skill set, it costs high to use 3rd party lib in long term and large scaled project.

3rd party lib is out of our control, over years, many of them died, or changed significantly, it's not trivial effort to keep up.

Usually popular 3rd party lib needs to cover variety of needs and becomes bloat, what we need is small portion,
but we have to take all complexity, sometimes the integration cost is higher than the actual implementation.
It becomes a problem when you have to dig into their implementation deeply to troubleshoot.

Many of unpopular 3rd party lib we used is actually maintained by single person or small team, and the quality and support varies which can be problematic.

For intrusive lib, like spring or hibernate, it impacts your design and architecture, any customization can be either impossible or convoluted to workaround.

For utility lib, it costs less to use and provides bigger value, but if we let its API spreads to many places,
it's hard to replace/update, and sometimes it's hard to let developers know which method to use, once new lib is imported, the usage is out of control.

As for the nature of our project, we have good control in infrastructure (AWS/GCloud) and technology stacks,
so we are able to solve the problem in the right place and use proper tools for each layers.
We would like to apply best practises of modern software development,
given that we think it is right choice to invest on our custom build framework and tech stack.

# Design principles

core-ng is designed and optimized for long term maintainability, code quality control. we prefer

* explicit over implicit
* type safety and refactory safety over dynamic code or code generation
* deterministic over dynamic flow

the goal of core-ng framework are:

* provides consistent API, developer can just use core.framework.api.* and focus on business logic
* use plain design, not twisting how to use underlying api, (tech changes fast, this is to make it easier to catch new features and do performance tuning)
* minimize the framework overhead, not to be bottleneck of performance issue, and be fast for both startup/runtime. (zero cost abstraction)
* minimize the 3rd party lib dependency, only use high quality ones.
* be cloud and operation friendly, provide enough tool for performance testing/troubleshooting
* zero config, pull all non-business related config out of app project
* embrace latest JAVA features and syntax

