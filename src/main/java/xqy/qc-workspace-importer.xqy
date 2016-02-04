xquery version "1.0-ml";

declare namespace qconsole="http://marklogic.com/appservices/qconsole";

import module namespace amped-qconsole = "http://marklogic.com/appservices/qconsole/util-amped" at "/MarkLogic/appservices/qconsole/qconsole-amped.xqy";
import module namespace idecl = "http://marklogic.com/appservices/qconsole/decl" at "/MarkLogic/appservices/qconsole/qconsole-decl.xqy";
import module namespace qconsole-model = "http://marklogic.com/appservices/qconsole/model" at "/MarkLogic/appservices/qconsole/qconsole-model.xqy";

declare namespace eval = "xdmp:eval";

declare variable $exported-workspace as xs:string external;
declare variable $user as xs:string external;

declare function local:qconsole-eval(
    $xquery as xs:string,
    $vars as item()*,
    $options as element(eval:options)?
) as item()*
{
    xdmp:security-assert("http://marklogic.com/xdmp/privileges/qconsole", "execute"),
    xdmp:eval($xquery, $vars, <options xmlns="xdmp:eval">
            <database>{xdmp:database("App-Services")}</database>
          </options>)
};

declare function local:import-workspace(
    $workspace as xs:string,
    $user as xs:string 
) 
{
    let $eval-query :=
       'declare namespace qconsole = "http://marklogic.com/appservices/qconsole";
        import module namespace qconsole-model="http://marklogic.com/appservices/qconsole/model"
            at "/MarkLogic/appservices/qconsole/qconsole-model.xqy";
        import module namespace amped-qconsole = "http://marklogic.com/appservices/qconsole/util-amped"
            at "/MarkLogic/appservices/qconsole/qconsole-amped.xqy";
        declare variable $xquery-query-template as xs:string external;
        declare variable $workspace as element(export) external;

        let $wsid := xdmp:random()
        let $imported-wsname := string($workspace/workspace/@name)
        let $existing-wsnames := amped-qconsole:qconsole-get-user-workspaces(())/qconsole:name/string()
        let $wsname :=
            if( $imported-wsname = $existing-wsnames )
            then qconsole-model:generate-workspace-name(())
            else $imported-wsname
        let $queries := $workspace/workspace/query
        let $userid := xdmp:get-current-userid()
        let $ws :=  <qconsole:workspace>
                        <qconsole:id>{$wsid}</qconsole:id>
                        <qconsole:name>{$wsname}</qconsole:name>
                        <qconsole:security>
                            <qconsole:userid>{$userid}</qconsole:userid>
                        </qconsole:security>
                        <qconsole:active>true</qconsole:active>
                        <qconsole:queries>
                            {
                            for $q at $i in $queries
                            let $qid := xdmp:random()
                            let $qname := string($q/@name)
                            let $focus := string($q/@focus)
                            let $active := string($q/@active)
                            let $content-source :=
                                if ( exists($q/@content-source) )
                                then string($q/@content-source)
                                else qconsole-model:default-content-source()
                            let $mode := string($q/@mode)
                            let $query-text := text { $q }
                            let $q-uri := concat("/queries/", $qid, ".txt")
                            let $save-q := amped-qconsole:qconsole-document-insert($q-uri, $query-text)
                            return
                            <qconsole:query>
                                <qconsole:id>{$qid}</qconsole:id>
                                <qconsole:name>{$qname}</qconsole:name>
                                <qconsole:content-source>{$content-source}</qconsole:content-source>
                                <qconsole:active>{$active}</qconsole:active>
                                <qconsole:focus>{$focus}</qconsole:focus>
                                <qconsole:mode>{$mode}</qconsole:mode>
                            </qconsole:query>
                            }
                        </qconsole:queries>
                    </qconsole:workspace>
        let $ws-uri := concat("/workspaces/", $wsid, ".xml")
        let $_ := xdmp:log(text{"workspace uri: ", $ws-uri})
        let $save-ws := amped-qconsole:qconsole-document-insert($ws-uri, $ws)
        let $_ := xdmp:log(text{"$save-ws: ", $save-ws})
        let $set-active := qconsole-model:set-only-one-workspace-active($wsid)

        return $wsid'
            
    let $workspace := xdmp:unquote($workspace)/node()        
    let $new-wsid := local:qconsole-eval($eval-query, (xs:QName("workspace"), $workspace, xs:QName("xquery-query-template"), $idecl:default-query-text), ())
    let $ws-uri := concat("/workspaces/", $new-wsid, ".xml")
    return $ws-uri
};

local:import-workspace($exported-workspace, $user)

