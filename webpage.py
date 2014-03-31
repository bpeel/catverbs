import verbdata
import cgi

def dump_conjugations(verb, title, name_prefix):
    if title:
        print("<h4>" + cgi.escape(title) + "</h4>")

    print("<p>")

    for name in ["jo", "tu", "ell", "nosaltres", "vosaltres", "ells"]:
        print(cgi.escape(verb.get_value(name_prefix + "_" + name)) + "<br />")

    print("</p>")

dictionary = verbdata.Dictionary()

print("""<!DOCTYPE html>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<title>Conjugació dels verbs catalans</title>
</head>
<body>
<h1>Conjugació dels verbs catalans</h1>""")

for verb in dictionary:
    print("<h2>" + cgi.escape(verb.get_value("infinitive")) + "<h2>")

    print("<h3>Indicatiu</h3>")

    dump_conjugations(verb, "Present", "pi")
    dump_conjugations(verb, "Imperfet", "ii")
    dump_conjugations(verb, "Passat simple", "spi")
    dump_conjugations(verb, "Futur", "future")
    dump_conjugations(verb, "Condicional", "cond")

    print("<h3>Subjuntiu</h3>")

    dump_conjugations(verb, "Present", "ps")
    dump_conjugations(verb, "Imperfet", "is")

    print("<h3>Imperatiu</h3>")

    dump_conjugations(verb, None, "imp")
    dump_conjugations(verb, "Imperfet", "is")

    print("<h3>Formes no personals</h3>\n"
          "<h4>Gerundi</h4>\n"
          "<p>" + cgi.escape(verb.get_value("gerund")) + "</p>\n"
          "<h4>Participi</h4>\n"
          "<p>\n" +
          cgi.escape(verb.get_value("m_participle")) + "<br />\n" +
          cgi.escape(verb.get_value("f_participle")) + "<br />\n" +
          cgi.escape(verb.get_value("pm_participle")) + "<br />\n" +
          cgi.escape(verb.get_value("pf_participle")) + "<br />\n"
          "</p>")

print("""</body>
</html>""")
