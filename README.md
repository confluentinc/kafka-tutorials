# Kafka Tutorials

The source code for the [Kafka Tutorials microsite](https://kafka-tutorials.confluent.io/). Read about it [in our blog post](https://www.confluent.io/blog/announcing-apache-kafka-tutorials).

## Setup

If you want to hack on this site to add a new tutorial or make a change, follow these instructions.

### Prerequisites

Make sure you have the following installed:

- ruby 2.3 or later
- [bundler](https://bundler.io/)
- npm
- python3 / pip3

On the Mac, you can get the dependencies like this:

```
brew install ruby node
gem install bundler
```

You'll now have an executable called harness-runner on your path. (Note that if you use Python, you likely already have the `pyyaml` package installed.)

### Installing

#### 1. Clone this repository

```
git clone git@github.com:confluentinc/kafka-tutorials.git
```

Then `cd` into the directory.

#### 2. Install the node packages

```
npm install
```

This will bring in some external JavaScript and CSS packages that we're using.

#### 3. Install the gems

```
bundle install
```

This will install Jekyll itself and any other gems that we use.

#### 4. Run the development server

```
bundle exec jekyll serve --livereload
```

This will launch a web server so that you can work on the site locally. Check it out on `http://localhost:4000`.

#### 5. Install the Pip package

This repository uses a Python package to facilitate testing the tutorials. To keep things simple, we bundled it into this repository. You can get everything you need by running the following:

```
pip3 install pyyaml
cd harness_runner
pip3 install -e .
```

## Add code for a new tutorial

A tutorial is a short procedure, targeted at developers, for getting a certain thing done using Confluent Platform.

In many cases, you can get that thing done using one of several _stacks_. For example, you might be able to perform data filtering by writing a KSQL query, by writing a Kafka Streams application, or by directly using the Kafka Consumer API. These comprise the three stacks this site supports: `ksql`, `kstreams`, and `kafka`.

Kafka Tutorials is a bit unique in that each tutorial is self-testing. That is, we have built a light-weight harness system that's able to instrument the code that belongs to each tutorial to make sure that it actually works. This is really useful as we expect to have a lot of tutorials.

With in each stack, these tutorials contain a few pieces. These are described below.

#### 1. Describe the problem your tutorial solves

The first thing to do is articulate what problem your tutorial is meant to solve. Every tutorial contains a problem statement and an example scenario. Edit `_data/tutorials.yml` and add your entry. The top item in this file represents the _short name_ for your tutorial. For example, the tutorial for transforming events of a stream is _transforming_. You'll also notice a `status` attribute. You can `enable` as many stacks as you'd like to author for this tutorial, but we recommend starting with just one.

#### 2. Make the directory structure

Next, make a few directories to set up for the project:

```
mkdir _includes/tutorials/<your tutorial short name>/<stack>/code
mkdir _includes/tutorials/<your tutorial short name>/<stack>/markup
```

#### 3. Write the code for the tutorial

Add your code for the tutorial under the `code/` directory you created. This should be entirely self-contained and executable with a `docker-compose.yml` file and a platform-appropriate build. Follow the conventions of existing tutorials as closely as possible.

At this point, you should feel free to submit a PR! A member of Confluent will take care of writing the markup and test files to integrate your code into the site. You can, of course, proceed to the next section and do it yourself, if you'd like.


## Add a narrative and test for the tutorial

This section is generally for those who work at Confluent and will be integrating new tutorials into the site. We need to do a little more work than just authoring the code. We also need to write the markup to describe the tutorial in narrative form, and also write the tests that we described to make sure it all works. This section describes how to do that.

#### 1. Create a harness for the tutorial

The harness is the main data structure that allows us to both test and render a tutorial from one form. Make a new directory under `_data/harnesses/` for your tutorial slug name and stack, like `_data/harnessess/<your tutorial short name>/ksql.yml`. Follow the existing harnesses to get a feel for what this looks like. The main thing to notice is that each step has a `render` attribute that points to a file. Create the markup for this in the next section.

#### 2. Create markup for the tutorial

Under the `markup/` directory that was created earlier, create 3 subdirectories: `dev`, `test`, and `prod`. Write the tutorial prose content here, following the conventions of existing tutorials. These files should be authored in Asciidoc.

#### 3. Tie it all together

Make a file named `/tutorials/<your tutorial short name>/<stack>.html`, specifying all the variables of interest. Note: the directory structure for these files is distinct from `/_includes/tutorials`.

For example, to display the tutorial with the ksqlDB stack:

```yml
# /tutorials/filter-a-stream-of-events/ksql.html
---
layout: tutorial
permalink: /tutorials/filter-a-stream-of-events/ksql
stack: ksql
static_data: filtering
---
```

You can do the same for Kafka Streams and Kafka, by using the `kstreams` and `kafka` stacks, respectively.

#### 4. Add your tutorial into build system

Lastly, create a Makefile in the `code` directory to invoke the harness runner and check any outputs that it produces. Then modify the `.semaphore/semaphore.yml` file to invoke that Makefile. This will make sure your tutorial gets checked by the CI system.

## Updating kafka-tutorials.confluent.io

The `release` branch tracks the content and code comprising the live site. Confluent manages the release process.

#### Prepare a release PR

A pull request into the `release` branch denotes a request to update the live site. The PR description should summarize the content changes and link to the staging site with the updates. In general, releases are timed, so target dates should also be noted.

Release artifacts are automatically promoted to the live site by CI, as part of successful `release` branch builds.
