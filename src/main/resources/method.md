#macro (printLine $line)
${line}
#if (! ${util.isCode($line)})

#end
#end

#macro (typeName $field)
$util.getTypeName($field.descriptor)##
#end

#macro (nestedMessage $message)
#foreach ($nested in ${context.getNestedMessages($message)})
${util.H(3)} ${nested.descriptor.name}
Field|Type|Description
-----|----|-----------
#foreach ($field in $nested.children)
${field.descriptor.name}|#typeName($field)|${field.commentInOneLine}
#end
#end
#end

${method.title}
====
#foreach ($commentLine in $method.comments)
#printLine(${commentLine})
#end

----------------
# URL
`${context.javaPackagePath}/${serviceName}/${method.descriptor.name}`

----------------
# Method
`${method.httpMethod}`

----------------
# Input
Field|Type|Description
-----|----|-----------
#foreach ($field in $input.children)
${field.descriptor.name}|#typeName($field)|${field.commentInOneLine}
#end

#foreach($commentLine in $input.comments)
#printLine(${commentLine})
#end

#nestedMessage($input)

${util.H(2)} Sample
${context.toJson(${input.descriptor})}

----------------
# Output
Field|Type|Description
-----|----|-----------
#foreach ($field in $output.children)
${field.descriptor.name}|#typeName($field)|${field.commentInOneLine}
#end

#foreach($commentLine in $output.comments)
#printLine(${commentLine})
#end

#nestedMessage($output)

${util.H(2)} Sample
${context.toJson(${output.descriptor})}