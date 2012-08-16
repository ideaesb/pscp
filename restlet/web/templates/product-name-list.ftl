<ul>
<#list table.names as name>
    <li><a href="${name.link}">${name.name}</a>
    <span>[ ${name.cnt} Products ]</span>
    </li>
</#list>
</ul>