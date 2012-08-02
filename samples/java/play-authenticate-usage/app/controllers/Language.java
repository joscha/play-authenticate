package controllers;

import play.Play;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class Language extends Controller {

    public static class Item {
        public String code;
        public int index;
        public String switchUrl;
        public Boolean active = false;
        public String name  ;

        public Item(int index, String code, String switchUrl, Boolean active, String name) {
            this.index = index;
            this.code = code;
            this.switchUrl = switchUrl;
            this.active = active;
            this.name = name;
        }
    }

    /**
     * Tries to determine default language based on users accept-languages
     *
     * @return Integer: Number of language allowed in application's conf
     */
    public static int getDefaultLang() {
        if (session("playmap_lang") == null) {

            String[] allowedList = Play.application().configuration().getString("application.langs").split(",");
            List<Lang> accepted = request().acceptLanguages();

            int langNumber = 0;
            boolean searchNext = true;
            for (Lang testLang : accepted) {
                if (searchNext) {
                    int testNumber = 0;
                    for (String s : allowedList) {
                        if (s.equals(testLang.code())) {
                            langNumber = testNumber;
                            searchNext = false;
                        }
                        testNumber++;
                    }
                }
            }
            session("playmap_lang", langNumber + "");
        }
        return Integer.parseInt(session("playmap_lang"));
    }

    /**
     * Fetches translated message basing on users default language. If no label found falldown to first language
     *
     * @param key Key of the message
     * @return Translated message
     */
    public static String message(String key) {
        String translated = Messages.get(Lang.availables().get(getDefaultLang()), key);
        if (translated.equals(key)) {
            translated = Messages.get(Lang.availables().get(0), key);
        }
        return translated;
    }

    /**
     * Fetches translated message basing on users default language. If no label found falldown to first language
     *
     * @param key     Key of the message
     * @param objects Additional objects
     * @return Translated message
     */
    public static String message(String key, Object... objects) {
        String translated = Messages.get(Lang.availables().get(getDefaultLang()), key, objects);
        if (translated.equals(key)) {
            translated = Messages.get(Lang.availables().get(0), key, objects);
        }
        return translated;
    }

    /**
     * Allows to change users language
     *
     * @param code Language's code
     * @return Redirect to main page
     */
    public static Result changeLanguage(String code, String returnpath) {

        Integer number = 0;

        for (int i = 0; i < Lang.availables().size(); i++) {
            if (Lang.availables().get(i).code().equals(code)) {
                number = i;
            }
        }

        if (Lang.availables().get(number) == null) {
            getDefaultLang();
        } else {
            session("playmap_lang", number.toString());
        }
        return redirect("/" + returnpath);
    }

    public static Result changeLanguageHome(String code) {
        return changeLanguage(code, "");
    }

    public static String changeLanguageLink(String code) {
        return routes.Language.changeLanguage(code, request().path().substring(1)).toString();
    }

    /**
     * Returns code of the selected language, ie.: en
     *
     * @return code
     */
    public static Item currentLanguageItem() {
        int i = getDefaultLang();
        return new Item(
                i,
                Lang.availables().get(i).code(),
                changeLanguageLink(Lang.availables().get(i).code()),
                true,
                getLanguageName(i)
        );
    }

    private static String getLanguageName(int i) {
        String[] languageNames = Play.application().configuration().getString("application.languageNames").split(",");
        String languageName = Lang.availables().get(i).code();
        if (languageNames[i]!=null){
            languageName=languageNames[i];
        }
        return languageName.trim();
    }

    public static List<Item> otherLanguageItems() {
        return languageItems(false);
    }

    public static List<Item> allLanguageItems() {
        return languageItems(true);
    }


    public static List<Item> languageItems(boolean allItems) {
        int currentId = getDefaultLang();
        List<Item> items = new ArrayList<Item>();

        for (int i = 0; i < Lang.availables().size(); i++) {

            if (i != currentId || allItems) {
                Item item = new Item(
                        i,
                        Lang.availables().get(i).code(),
                        changeLanguageLink(Lang.availables().get(i).code()),
                        (currentId == i),
                        getLanguageName(i)
                );
                items.add(item);
            }
        }
        return items;
    }
}
