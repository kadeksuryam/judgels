package judgels.michael.problem.base.statement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import judgels.michael.template.HtmlTemplate;
import judgels.michael.template.TemplateView;
import judgels.sandalphon.resource.WorldLanguageRegistry;

public class EditStatementView extends TemplateView {
    private final String language;
    private final Set<String> enabledLanguages;

    public EditStatementView(
            HtmlTemplate template,
            EditStatementForm form,
            String language,
            Set<String> enabledLanguages) {

        super("editStatementView.ftl", template, form);
        this.language = language;
        this.enabledLanguages = enabledLanguages;
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, String> getEnabledLanguages() {
        Map<String, String> languages = new LinkedHashMap<>();
        for (String lang : enabledLanguages) {
            languages.put(lang, WorldLanguageRegistry.getInstance().getLanguages().get(lang));
        }
        return languages;
    }
}
