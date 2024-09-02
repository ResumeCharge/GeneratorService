module Jekyll
  module UrlFilter
    def starts_with_http_or_https(string)
      matches = /h/.match(string)
      matches != nil
    end
    def starts_with_www(string)
      string.start_with?("www.")
    end
    def replace_www_with_https(string)
      string.sub!("www.", "https://")
      string
    end
    def add_https_prefix(string)
      string.prepend("https://")
      string
    end
    def url_filter(input)
      if starts_with_http_or_https(input)
        return input
      end
      if starts_with_www(input)
        return replace_www_with_https(input)
      end

      add_https_prefix(input)
    end
  end
end

Liquid::Template.register_filter(Jekyll::UrlFilter)