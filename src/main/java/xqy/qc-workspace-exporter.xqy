xquery version "1.0-ml";

declare namespace qconsole="http://marklogic.com/appservices/qconsole";

declare variable $user as xs:string external;
declare variable $workspace as xs:string external;

declare function local:do-eval($query as xs:string, $vars) {
  xdmp:eval($query, $vars, 
      <options xmlns="xdmp:eval">
      <database>{xdmp:database("App-Services")}</database>
      </options>)
};

declare function local:get-ws-uri($user as xs:string, $workspace as xs:string) {
  let $ws-query := 'xquery version "1.0-ml";
    declare namespace qconsole = "http://marklogic.com/appservices/qconsole";
    declare variable $user as xs:string external;
    declare variable $workspace as xs:string external;
    cts:uris((), (), cts:and-query((
        cts:directory-query("/workspaces/"),
        cts:element-value-query(xs:QName("qconsole:userid"), xs:string(xdmp:user($user))),
        cts:element-value-query(xs:QName("qconsole:name"), $workspace)
    ))
    )'
  return local:do-eval($ws-query, (xs:QName("user"), $user, xs:QName("workspace"), $workspace))
};

declare function local:get-workspace($ws-uri as xs:string) {
  let $query := "declare variable $ws-uri as xs:string external; fn:doc($ws-uri)"
  return local:do-eval($query, (xs:QName("ws-uri"), $ws-uri))
};

let $user := ($user, xdmp:get-current-user())[1]

let $ws-uri := local:get-ws-uri($user, $workspace)
let $ws := local:get-workspace($ws-uri)
let $queries := 
    for $q in $ws/qconsole:workspace/qconsole:queries/qconsole:query
    return 
      <query name="{string($q/qconsole:name)}" focus="{string($q/qconsole:focus)}" active="{string($q/qconsole:active)}" mode="{string($q/qconsole:mode)}">
        {local:do-eval(concat("fn:doc('/queries/", xs:unsignedLong($q/qconsole:id), ".txt')"), ())}
      </query>

let $export := 
    if ($queries) then (
    <export>
      <workspace name="{string($ws/qconsole:workspace/qconsole:name)}">
        {$queries}
      </workspace>
    </export> )
    else (text{"No workspace found with the name of ", $workspace, "."})

return $export