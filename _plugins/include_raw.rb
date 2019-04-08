module Jekyll
  module Tags
    class IncludeRawTag < IncludeTag
      include Filters

      def read_file(file, context)
        "{% raw %}#{xml_escape(super(file, context))}{% endraw %}"
      end
    end
  end
end

Liquid::Template.register_tag("include_raw", Jekyll::Tags::IncludeRawTag)
