build:
	make -C _includes/recipes/filtering/kstreams/code uberjar

recipe_outputs:
	make -C _includes/recipes/filtering/kstreams/code recipe_outputs

test:
	make -C _includes/recipes/filtering/kstreams/code test
