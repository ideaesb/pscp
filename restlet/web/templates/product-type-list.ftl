<ul>
<#list table.types as type>
    <li><a href="producttypes/${type.typeid}">${type.typenum} - ${type.typename}</a>
    <span>[ ${type.cnt} Products ] </span>
    <a href="${webroot}about/${type.typenum}.html">[ About ]</a>
    </li>
</#list>
</ul>