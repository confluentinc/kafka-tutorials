# Confluent Developer

The developer microsite for Confluent.

## Getting Started

### Prerequisites

Make sure you have the following installed:

- ruby
- [bundler](https://bundler.io/)
- npm

### Installing

#### 1. Clone this repository

```
git@github.com:confluentinc/confluent-developer.git
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

This will install Jekyll itself.

#### 4. Run the development server

```
jekyll serve
```

This will launch a web server so that you can work on the site locally. Check it out on `http://localhost:4000`.

### Add a new recipe

#### 1. Make the directory structure

```
mkdir _includes/recipes/<your recipe short name>/<stack>/code
mkdir _includes/recipes/<your recipe short name>/<stack>/harness
mkdir _includes/recipes/<your recipe short name>/<stack>/markup
```

Recipe short-name can be anything meaningful to uniquely identify the recipe. Stack name should be `ksql`, `kstreams`, or `kafka`.

#### 2. Write the code for the recipe

Add your code for the recipe under the `code/` directory you created. This should be entirely self-contained and executable. Follow the conventions of existing recipes as closely as possible.

#### 3. Create markup for the recipe

Under the `markup/` directory, create 3 files: `try_it.html`, `test_it.html`, and `take_it_to_prod.html`. Write the recipe prose content here, following the conventions of existing recipes.

#### 4. Tie it all together

Make a file under the `recipes` directory, specifying all the variables of interest. To support a stack, add the trio of variables to the respective markup. For example, to display the recipe with KSQL:

```
ksql_try_it: recipes/filtering/ksql/markup/try_it.html
ksql_test_it: recipes/filtering/ksql/markup/test_it.html
ksql_take_it_to_prod: recipes/filtering/ksql/markup/take_it_to_prod.html
```

You can do the same for Kafka Streams and Kafka, by using the `kstreams_` and `kafka_` prefixes, respectively. These should all point to files that we'll describe next.

#### 5. Write a test

Since this is a self-testing site, add a test to make sure your recipe's content works. Add your test to the `harness` directory, following how existing recipes do this. Create a `Makefile` with a target called `recipe` to build the content and run the tests.

#### 6. Tie into the top-level Makefile

Modify the `Makefile` at the root of the repository to run your recipe as part of the build along with all the others.
