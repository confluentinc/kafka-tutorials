.PHONY: recipes

recipes:
	make -C _includes/recipes/filtering/ksql/harness recipe
	make -C _includes/recipes/filtering/kstreams/harness recipe
	make -C _includes/recipes/splitting/kstreams/harness recipe
	make -C _includes/recipes/merging/kstreams/harness recipe
	make -C _includes/recipes/transforming/kstreams/harness recipe
