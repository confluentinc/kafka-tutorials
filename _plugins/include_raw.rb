module Jekyll
  module Tags
    class IncludeRawTag < IncludeTag
      def read_file(file, context)
        "{% raw %}#{super(file, context)}{% endraw %}"
      end
    end
  end
end

Liquid::Template.register_tag("include_raw", Jekyll::Tags::IncludeRawTag)
