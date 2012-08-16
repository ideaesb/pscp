<#macro formTable>
    <form action="" method="post">
        <table>
            <#nested>
            <tr>
                <td colspan='2'>
                    <input type='submit'/>
                </td>
            </tr>
        </table>
    </form>
</#macro>

<#macro inputFile field size="15" required=false>
    <div class='row'>
        <#nested>
        <div>
            <@doLabel field required/>
            <input required="${required?string}" name="${field.name}" id="${field.name}" type="file"/>
        </div>
    </div>
</#macro>

<#macro inputTextRow field default="" size="15" required=false>
    <tr>
        <td>${field.label}</td>
        <td><@doInputText field default size required/></td>
    </tr>
</#macro>

<#macro inputText field default="" size="15" required=false>
    <div class='row'>
        <#nested>
        <div>
            <@doLabel field required/>
            <@doInputText field default size required/>
        </div>
    </div>
</#macro>

<#macro doLabel field required>
    <#if required>
        <#local reqel="<span class='required'>*</span>"/>
   </#if>
    <label for="${field.name}">${field.label}${reqel!}</label>
</#macro>

<#macro doInputText field default="" size="15" required=false>
    <#assign acURL=''/>
    <#if field.acURL??>
        <#assign acURL="acurl='${field.acURL}'"/>
    </#if>
    <input required="${required?string}" id="${field.name}" size="${size}" type="text" ${acURL!} name="${field.name}" value="${field.value!default}"/>
</#macro>

<#macro inputArea field default="" cols="30" rows="4" required=false>
    <div class='row'>
        <@doLabel field required/>
        <textarea id="${field.name}" rows="${rows}" cols="${cols}">${field.value!default}</textarea>
    </div>
</#macro>

<#macro inputRadioRow field default="">
    <tr>
        <td>${field.label}</td>
        <td>
        <@doInputRadio field default/></td>
    </tr>
</#macro>

<#macro inputRadio field default="">
    <div class='row'>
        <#nested>
        <div>
            <label for="${field.name}">${field.label}</label>
            <div class="input">
                <@doInputRadio field default/>
            </div>
            <div class="endrow"></div>
        </div>
    </div>
</#macro>

<#macro doInputRadio field default="">
    <#list field.params as param>
        <#if (param.value == field.value!default) || (param.name == field.value!default)>
            <#assign checked="checked='checked'">
        <#else>
            <#assign checked="">
        </#if>
        <div><input type="radio" name="${field.name}" value="${param.value}" ${checked!""}/>${param.name}</div>
    </#list>
</#macro>

<#macro select field default="" size="10" width="15em">
    <div class='row'>
        <#nested>
        <div>
            <label for="${field.name}">${field.label}</label>
            <select id="${field.name}" name="${field.name}" size="${size}" style="width:${width}">
                <#list field.params as param>
                    <#if (param.value == field.value!default) || (param.name == field.value!default)>
                        <#assign selected="selected='selected'">
                    <#else>
                        <#assign selected="">
                    </#if>
                    <option value="${param.value}" ${selected!""}>${param.name}</option>
                </#list>
            </select>
        </div>
    </div>
</#macro>

<#macro checkbox field default="false">
    <div class='row'>
        <#nested>
        <div>
            <label for="${field.name}">${field.label}</label>
            <#if (field.value!default) == "true">
                <#assign selected="checked='true'">
            <#else>
                <#assign selected="">
            </#if>
            <input type="checkbox" id="${field.name}" name="${field.name}" ${selected}/>
        </div>
    </div>
</#macro>

<#macro hidden field default="">
    <input id="${field.name}" type="hidden" name="${field.name}" value="${field.value!default}"/>
</#macro>