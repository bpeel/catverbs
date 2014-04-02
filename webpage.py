import verbdata
import cgi
import os
import errno

def dump_conjugations(f, verb, title, name_prefix):
    if title:
        f.write("<h3>" + cgi.escape(title) + "</h3>\n")

        f.write("<p>\n")

    for name in ["jo", "tu", "ell", "nosaltres", "vosaltres", "ells"]:
        f.write(cgi.escape(verb.get_value(name_prefix + "_" + name)) +
                "<br />\n")

    f.write("</p>\n")

dictionary = verbdata.Dictionary()

try:
    os.mkdir("html")
except OSError as e:
    if e.errno != errno.EEXIST:
        raise

infinitives = []

for verb in dictionary:
    infinitive = verb.get_value("infinitive")
    infinitives.append(infinitive)

    f = open(os.path.join("html", infinitive + ".html"), "w", encoding="UTF-8")

    f.write("<!DOCTYPE html>\n"
            "<html>\n"
            "<head>\n"
            "<meta http-equiv=\"content-type\" "
            "content=\"text/html; charset=utf-8\">\n"
            "<title>" + cgi.escape(infinitive) + "</title>\n"
            "</head>\n"
            "<body>\n"
            "<h1>" + cgi.escape(infinitive) + "</h1>\n")

    f.write("<h2>Indicatiu</h2>\n")

    dump_conjugations(f, verb, "Present", "pi")
    dump_conjugations(f, verb, "Imperfet", "ii")
    dump_conjugations(f, verb, "Passat simple", "spi")
    dump_conjugations(f, verb, "Futur", "future")
    dump_conjugations(f, verb, "Condicional", "cond")

    f.write("<h2>Subjuntiu</h2>\n")

    dump_conjugations(f, verb, "Present", "ps")
    dump_conjugations(f, verb, "Imperfet", "is")

    f.write("<h2>Imperatiu</h2>\n")

    dump_conjugations(f, verb, None, "imp")
    dump_conjugations(f, verb, "Imperfet", "is")

    f.write("<h2>Formes no personals</h2>\n"
            "<h3>Gerundi</h3>\n"
            "<p>" + cgi.escape(verb.get_value("gerund")) + "</p>\n"
            "<h3>Participi</h3>\n"
            "<p>\n" +
            cgi.escape(verb.get_value("m_participle")) + "<br />\n" +
            cgi.escape(verb.get_value("f_participle")) + "<br />\n" +
            cgi.escape(verb.get_value("pm_participle")) + "<br />\n" +
            cgi.escape(verb.get_value("pf_participle")) + "<br />\n"
            "</p>\n")

    f.write("</body>\n"
            "</html>\n")

    f.close()

f = open(os.path.join("html", "index.html"), "w", encoding="UTF-8")

f.write("<!DOCTYPE html>\n"
        "<html>\n"
        "<head>\n"
        "<meta http-equiv=\"content-type\" "
        "content=\"text/html; charset=utf-8\">\n"
        "<title>Conjugació dels verbs catalans</title>\n"
        "</head>\n"
        "<body>\n"
        "<h1>Conjugació dels verbs catalans</h1>\n"
        "<ul>\n")

for infinitive in sorted(infinitives):
    f.write("<li><a href=\"" + cgi.escape(infinitive) + ".html\">" +
            cgi.escape(infinitive) + "</a></li>\n")

f.write("</ul>\n"
        "</body>\n"
        "</html>\n")
