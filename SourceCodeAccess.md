# Introduction #

As of Parallel Sets 2.1, we no longer provide a downloadable source code package. This is an active development project, so it makes little sense to download a static source package that you cannot easily update later. Using Mercurial to download and update source code is very simple. Also, if you are tech-savvy enough to look at Java source code, you should be able to pick up a new version control system (if you don't know it already).

# Software Needed #

  * [Eclipse](http://eclipse.org/), the IDE
  * [Mercurial (hg)](http://hg-scm.org/), the version control system.
  * [MercurialEclipse](http://www.vectrace.com/mercurialeclipse/), the plugin that lets Eclipse talk to Mercurial.

# Cloning the Repository #

In distributed version control systems like Mercurial, you don't check out from a server, but you clone the repository. That means you get a complete copy of the entire repository on your machine. You can then check out any revision you want at any time, without depending on the server.

In Eclipse, once you have installed MercurialEclipse, you can go to _File->Import ..._ and pick _Clone repository using MercurialEclipse_ under _MercurialEclipse_ in the list. Click _Next_, and enter `https://parsets.googlecode.com/hg/` as the repository URL. Click _Finish_. Done.

To check for updates, right-click the project and select _Team->Pull ..._ from the menu. The URL will already be there, you just need to click _Finish_.

# Access without Eclipse #

If you don't want to use Eclipse, you can check out the project from the command line. The [source checkout page](http://code.google.com/p/parsets/source/checkout) gives you instructions how to do that.

To update the project, perform these commands in the Parallel Sets directory: `hg pull; hg update`

On Linux and Mac OS X, you can also install Mercurial from the command line using this command: `sudo easy_install mercurial`