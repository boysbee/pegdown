package org.pegdown;

import org.parboiled.common.StringUtils;
import org.pegdown.ast.*;

import static org.pegdown.FastEncoder.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A LinkRenderer is responsible for turning an AST node representing a link into a {@link LinkRenderer.Rendering}
 * instance, which hold the actual properties of the link as it is going to be rendered.
 * If you'd like to apply custom logic to link rendering (e.g. for selectively adding "nofollow" attributes) you
 * should derive a custom LinkRenderer from this class and override the respective methods.
 */
public class LinkRenderer {

    /**
     * Simple model class for an HTML tag attribute.
     */
    public static class Attribute {
        public static final Attribute NO_FOLLOW = new Attribute("rel", "nofollow");
        public final String name;
        public final String value;

        public Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Simple model class for holding the `href`, link text as well as other tag attributes of an HTML link.
     */
    public static class Rendering {
        public final String href;
        public final String text;
        public final List<Attribute> attributes = new ArrayList<Attribute>(2);

        public Rendering(String href, String text) {
            this.href = href;
            this.text = text;
        }

        public Rendering withAttribute(String name, String value) {
            return withAttribute(new Attribute(name, value));
        }

        public Rendering withAttribute(Attribute attr) {
            attributes.add(attr);
            return this;
        }
    }

    public Rendering render(AnchorLinkNode node) {
        String name = node.getName();
        return new Rendering('#' + name, node.getText()).withAttribute("name", name);
    }

    public Rendering render(AutoLinkNode node) {
        return new Rendering(node.getText(), node.getText());
    }

    public Rendering render(ExpLinkNode node, String text) {
        Rendering rendering = new Rendering(node.url, text);
        return StringUtils.isEmpty(node.title) ? rendering : rendering.withAttribute("title", encode(node.title));
    }

    public Rendering render(ExpImageNode node, String text) {
        Rendering rendering = new Rendering(node.url, text);
        return StringUtils.isEmpty(node.title) ? rendering : rendering.withAttribute("title", encode(node.title));
    }

    public Rendering render(MailLinkNode node) {
        String obfuscated = obfuscate(node.getText());
        return new Rendering("mailto:" + obfuscated, obfuscated);
    }

    public Rendering render(RefLinkNode node, String url, String title, String text) {
        Rendering rendering = new Rendering(url, text);
        return StringUtils.isEmpty(title) ? rendering : rendering.withAttribute("title", encode(title));
    }

    public Rendering render(RefImageNode node, String url, String title, String alt) {
        Rendering rendering = new Rendering(url, alt);
        return StringUtils.isEmpty(title) ? rendering : rendering.withAttribute("title", encode(title));
    }

    public Rendering render(WikiLinkNode node) {
        try {
            // vsch: #182 handle WikiLinks alternative format [[page|text]]
            String text = node.getText();
            String url = text;
            int pos;
            if ((pos = text.indexOf("|")) >= 0) {
                url = text.substring(0, pos);
                text = text.substring(pos+1);
            }

            url = "./" + URLEncoder.encode(url.replace(' ', '-'), "UTF-8") + ".html";
            return new Rendering(url, text);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }
}
