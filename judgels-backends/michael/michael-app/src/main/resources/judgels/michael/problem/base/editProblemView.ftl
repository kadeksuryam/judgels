<#-- @ftlvariable type="judgels.michael.problem.base.EditProblemView" -->

<#import "/judgels/michael/template/templateLayout.ftl" as template>

<#import "/judgels/michael/template/form/forms.ftl" as forms>

<@template.layout>
  <@forms.form>
    <h3>Info</h3>
    <@forms.text name="slug" label="Slug" value=form.slug required=true pattern="[a-z0-9]+(-[a-z0-9]+)*" title="Slug can only consist of alphanumerics and dashes"/>
    <@forms.textarea name="additionalNote" label="Additional note" value=form.additionalNote/>

    <h3>Metadata</h3>
    <@forms.text name="writerUsernames" label="Writers" value=form.writerUsernames help="List of comma-separated usernames."/>
    <@forms.text name="developerUsernames" label="Developers" value=form.developerUsernames help="List of comma-separated usernames. Fill only if different from writers."/>
    <@forms.text name="testerUsernames" label="Testers" value=form.testerUsernames help="List of comma-separated usernames."/>
    <@forms.text name="editorialistUsernames" label="Editorialists" value=form.editorialistUsernames help="List of comma-separated usernames."/>

    <@forms.submit>Update</@forms.submit>
  </@forms.form>
</@template.layout>
