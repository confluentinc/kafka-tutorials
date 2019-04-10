# Confluent Developer

The developer microsite for Confluent.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Make sure you have the following installed:

- ruby
- [bundler](https://bundler.io/)
- npm

### Installing

1. Clone this repository

```
git@github.com:confluentinc/confluent-developer.git
```

Then `cd` into the directory.

2. Install the node packages

```
npm install
```

This will bring in some external JavaScript and CSS packages that we're using.

3. Install the gems

```
bundle install
```

This will install Jekyll itself.

4. Run the development server

```
jekyll serve
```

This will launch a web server so that you can work on the site locally. Check it out on `http://localhost:4000`.