PSCP Developer Docs
===================

.. contents:: Table of Contents

.. sectnum::


Requirements
------------

* Java 1.6
* Tomcat 5.5
* Postgres 8.3
* Apache 2.2
* PHP (not sure of version, nothing fancy being used though)
* PHP Postgres Library (not sure of real name)

Dev Environment
---------------

Developed using Netbeans IDE, though certainly not required. Netbeans does
support PHP and Java projects along with good javascript and css support.

During development, I also ran on Linux when working at home. There are
no specific blocks to being developed or deployed on Linux.

The Restlet project contains the build files for local deployment and setup
and supports packaging things up for Uday. See below in `Deployment`_.

Setup
~~~~~

Check out subversion repository at::

  svn://jackson/svn/rtisw/trunk/ProductMetaData/webapp.pscp

Check the build script and properties for assumptions on postgres login and
password. 

Workflow
~~~~~~~~

For PHP work, the development cycle is simple - edit, refresh browser, test.

When ready, deploy to testing server (like williamsfork).

For the Restlet work, a similar cycle is used though deployment is slightly
slower and eventually Tomcat will need to be completely cycled (it slowly
'leaks' memory during application reloading).

Deployment
~~~~~~~~~~

The `build.xml` file in the restlet project contains targets to deploy local
and to Uday. 

The main targets used for local deployment are:

* deploy-williamsfork (both restlet and php)
* php-williamsfork (php only)
* postgres-* (database management)

For Uday, the ``ftp`` target is used. Note the MD5 sums that are printed out
since Uday feels better being able to verify things (and you can convince him
he did something wrong when he argues with you regarding something not working
and insinuating you did not upload the right files or something). 

Between the target names, descriptions and contents, the rest of the targets
should be self explanatory. 

PHP Project
-----------

This part of the project was derived from what we were handed initially (the
code from the old PRICIP web site). This uses a home grown (or maybe `obtained
from the internet and modified`) templating type framework to put content
together. 


Other Thoughts
--------------

Admin Interface
~~~~~~~~~~~~~~~

The 'admin' interface, while somewhat useful, took a while to develop and
is one of the more complicated aspects of the site, especially because I
thought that various contributers would be using the site and wanted to ensure
secure authentication (at least on the transport side of things) by using
http digest auth. This is problematic since doing digest auth requires access
to the raw password. Passwords are not stored raw in the database, but are
encrypted using two-way encryption which means someone could theoretically
reverse them. A better approach would be to allow a single 'admin' account not
attached to any real user or email (preventing stolen credentials from being
useful elsewhere) and use the existing REST endpoints for admin but driven
by a different client (like a command line or...) and not the complicated
one in use now.

Restlet/PHP
~~~~~~~~~~~

The confusing and painful part of this relationship is that content is provided
by both frameworks - static content by PHP and database 'dynamic' content by
the Restlet components using `freemarker` templates. Freemarker, while not
useless, is harder to code in since the template language and syntax coloring
don't go too well together. Therefore, the freemarker generated content is
limited to simple markup and included in the page(s) via AJAX.

Javascript
~~~~~~~~~~

The Javascript could be rewritten to be simpler. The most complicated aspect
is the 'rich history' - the back and forward button even though the page is not
reloaded. I can't explain how this works here, but it is well documented on the
web in other frameworks, etc.

DAO Layer
~~~~~~~~~

For better or worse, the DAO layer is homegrown. It is akin to a DAO/DTO/VO
system. Instead of Value Objects with getters and setters, it uses a property
like system with generic row objects. This works well when getting data and
passing it to the template layer, but has drawbacks elsewhere. It was designed
to be reused in other applications, but has not been yet.

Evaluate replacement based on amount of additional work to schema.

