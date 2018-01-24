# 4.1.8 (2017-10-24)

## Bug fixes

- fix grouping on date/datetime fields
- fix default values resetting current record values
- fix typo in open-sans.css

# 4.1.7 (2017-10-10)

## Bug fixes

- fix regression causing onLoad called on new record
- fix x-bind causing dirty flag issue
- encrypt all plain text passwords at server startup after module initialization.
- fix IE font loading issues
- fix regression causing values lost in popup
- reload grid view is switching back from non-form view

# 4.1.6 (2017-10-03)

## Bug fixes

- fix file upload issue on IE11
- remove plain text password match
- fix french translation
- fix relational fields permission issues
- fix regression caused by invalid value in editor

# 4.1.5 (2017-09-22)

## Improvements

- consider X-Forwarded-* headers when preparing base url
- optimize menu loading

## Bug fixes

- fix hilite issue
- fix multi-value field editor issue
- fix menu groups issue
- fix menu permission check issue
- fix multiple view popup rendering issue
- fix expression with leading whitespace issue
- fix wrong form items matching
- fix DMS permission issues
- fix recursive gantt when a task refer itself as parent task
- fix o2m editor issues with invalid fields
- fix o2m editor fields loosing values
- fix master-details widget causing overflow

# 4.1.4 (2017-06-19)

## Bug fixes

- fix dotted fields update issue
- do not throw unused IOException
- isolate dashlet grid columns to avoid name conflict 
- fix unnecessary dirty flag caused by validators
- fix unnecessary dirty check when create new record

# 4.1.3 (2017-05-16)

## Bug fixes

- fix dotted field update issue
- fix domain support issue on ref-select widget
- fixed translation loading issue

# 4.1.2 (2017-03-20)

## Bug fixes

- fix button prompt issue in grid view
- fix wrong record selected with grouped selection popup
- fix BIRT report pdf word wrap issue
- fix decimal formatting issue in grid view
- fix alignment issue on grouped grid
- fix __check_version use on tabs opened with edit-window="blank"
- fix __check_version reload ignores dirty checking

# 4.1.1 (2017-02-21)

## Bug fixes

- fix NPE issue on xml data import when text node is empty
- fix x-precision and x-scale attributes issue
- fix legend display issue on calendar view
- fix editable grid issue with selection checkbox
- fix technical help info lost issue
- fix binary field issue if form has onSave
- fix attachment file name encoding issue

## Improvements

- add support for changing page limit on search dialog
- allow to define number of rows display in relational field
- allow to copy record line on master detail widget

# 4.1.0 (2016-12-15)

## Bug fixes

- fix html field issue in grid widget
- fix tree view dashlet reload issue
- fix various nested editor issues
- fix edit-window attribute on panel-related
- fix pagination issue
- fix m2o with canView=false not working
- fix forceEdit view-param to open record in edit mode
- fix wrong alignment of fields wWhen we show/hide first field in a row dynamically
- fix grid view context issue for readonlyIf condition
- fix dashlet dynamic title change issue
- fix domain context update issue on chart dashlets
- fix date/datetime default value (default="now")
- fix Entity.equals and HashKey annotation issues
- fix view loader for unresolved imports : check only once all modules are installed
- fix dummy field update issue
- fix selection flag issue in nested o2m
- fix 'save' action that should always save main record
- fix missing variable $user & $group in expression evaluator
- fix hilite in kanban view
- fix tag-select widget issue
- fix label style of inline boolean widget
- fix ref-select widget issues
- fix colorBy issue in calendar view
- fix editable dummy field issue
- fix i18n extract text from static and help fields
- fix unnecessary call to onLoad action
- fix grid column title reset issue
- fix advance search state lost issue
- fix duplicate onChange action and select request on o2m
- fix app locale issue
- fix dirty record on copy
- fix dms view issue with document/spreadsheet
- fix selection field issue with wrong search text
- fix zIndex issue on grid's filter inputs
- fix multiline cdata text of static/help extraction issue
- fix license headers issue if path has "data" folder
- fix default values issue on form view
- fix reload root record with actions
- fix calendar view not considering end date when feching items
- fix grid widget onChange issue
- fix missing title on selector popup
- fix calendar view legends clipped issue
- fix height attribute issue on text field
- fix boolean tracking value
- fix Html and Text widget validation issues
- fix selection ordering issue
- fix 'user.lang' app info
- fix missing adapters for CSV sub binding
- fix scaffolding templates
- fix 'tag-get' preference over 'tag-count'
- fix menu tag display issue
- fix field permission readonlyIf condition conflict
- fix onChange issue on date widget
- fix dotted fields support in field editor
- fix issue when creating new user with custom button
- fix readonly calendar view issue
- fix chart rendering on missing value
- fix readonly button issue in grid widget
- fix js expression not trigger on editor field
- fix editor with panel not loading nested values
- fix action-attrs issues on editor used as viewer
- fix code widget keyboard shortcut conflict
- fix selection widget issue in firefox
- fix save DMS permission NPE issue
- fix x-lite attribute on html field
- dix default values handler causing widget state reset

## Improvements

- improve visibility of boolean values in grid
- add support for archived records in search view
- allow to select search view objects with actions
- allow helpLink all views
- add sidebar attribute support on dashlet
- change default gantt view mode to "week"
- improve CORS support
- show different icon for view only popup on m2o
- allow to open record on kanban item click
- add 'no records found' message to kanban view
- use same js expression helpers for custom views
- improve load more records button design
- style disabled btn-link in form view
- improve calendar bubble content
- do not allow sorting of transient fields
- don't log actions ending with MenuTag()
- implement field reset error feature
- don't open relational field in new tab if currently in popup
- apply license header on generate code
- add support for onLoad actions on multi-object search
- generate entity base class MetaField instances
- return 0 as hashCode if no hashKey defined
- improve default views
- don't save translated title of default tracking subject
- add priority field in MetaSelect views
- add support to disable kanban drag&drop feature
- improve calendar locale based on user lang
- implement CSV search call feature
- upgrade postgresql-jdbc 9.4.1212
- add support for setting query readOnly
- add some optimization on search and count queries
- do not add empty editor on o2m if x-show-on-new is false
- improve datetime field validation
- do not try to switch back is no other views found
- allow enter key to update page size

# 4.0.0 (2016-05-09)

Tones of new features, new refreshed look and feel and more.

## Improvements

- support for MySQL 5.x (including MariaDB, WebScaleSQL)
- centralized document management
- improved file upload & attachment management
- more consistent web services
- implemented field value translation
- implemented accent neutral search
- improved mail api (smtp, imap, pop3 support)
- java expression language support for action expressions
- better request context handling
- implemented role based menu permissions
- implemented new views (kanban, cards, custom html, gantt)
- support for change tracking and messaging
- support for re-sequence grid items with drag & drop
- support for selection list enhancement from modules
- support for view field permissions
- support for CAS authentication
- support for CORS
- upgraded to angular 1.4 (better performance and url handling)
- new default theme
- implemented per user theme support
- implemented new improved navigation menu (icons, colors, tags)
- support for single tab ui
- support for extending index.jsp with include templates
- replaced tinymce widget with new lightweight HTML widget
- implemented new widgets (BinaryLink, BooleanSelect, BooleanRadio, BooleanSwitch)
- implemented new funnel chart
- support for excluding fields when duplicating a record
- support for custom chart colors
- support for favourite menu (bookmarks)
- implemented new checkbox-select widget
- implemented new static text widget
- implemented new static help widget

and lot more...

## Database (column name changes)

- meta_translation (key -> message_key, message -> message_value)
- meta_file (size -> file_size, type -> file_type, size_text -> file_size_text)
- permission (condition -> condition_value)

## Bug fixes

Almost all fixes in v3.x branch are merged in v4.x branch. See git log for more comprehensive
list of changes.

# 3.1.4 (2016-04-13)

## Bug fixes

- fix open view as popup
- fix "close" action validation
- fix pagination issue on dashlet grid
- fix dashlet refresh issue
- fix dashlet context issue
- fix jquery-ui css conflict

# 3.1.3 (2016-03-25)

## Bug fixes

- fix jquery-ui css conflict
- fix html widget update issue
- fix view switching issue in popup
- fix empty space left by hidden widgets
- fix view switching issue in popup
- fix action-attrs issue on toolbar

# 3.1.2 (2016-03-10)

## Improvements

- support for order/limit of suggest box list
- implement code view support for html widget
- add button to normalize html value

## Bug fixes

- normalize html widget content display
- fix view by groups issue

# 3.1.1 (2016-02-26)

## Improvements

- allow to open url actions in new tab

## Bug fixes

- fix wrong form items matching
- fix action handler polluting response data
- fix value update with actions issue
- fix recursion issue when using o2m in of same type in grid view
- fix wrong escape in groovy template when using closure

# 3.0.25 (2016-02-26)

## Bug fixes

- fixed date widget clear value problem
- fix wrong form items matching
- fix action handler polluting response data
- fix value update with actions issue

# 3.1.0 (2016-02-12)

There are many bug fixings and minor improvements in 3.0.x release.
It's time to move forward with a new minor release.

## Improvements

- added support for excluding fields to copy
- add css rule to attach dashlet panels
- backported new html widget from v4

## Bug fixes

- fixed panel toggle issue caused by collapse animation
- fixed date widget clear value problem
- fixed context issues

# 3.0.24 (2016-02-12)

## Improvements

- added support for excluding fields to copy
- add css rule to attach dashlet panels
- backported new html widget from v4

## Bug fixes

- fixed panel toggle issue caused by collapse animation
- fixed date widget clear value problem
- fixed context issues

# 3.0.23 (2016-01-27)

## Improvements

- allow buttons in field editor
- addded helpLink field to MetaView
- support for one-to-one field in advance search
- support for special action "close"
- support for special action "new"
- show html content in grid cell
- notify to refresh browser after meta data reload
- automatically detach orphaned one-to-many records

## Bug fixes

- fix wrong record in form view
- fix showTitle attribute issue on editor fields
- fix field alignment issue
- fix field editor as viewer is not reused
- fix id value not persisted for selections
- fix date input widget issue
- fix i18n bundle cache update issue
- fix nested selection field issue in tree view
- fix mail builder issue for multi-part messages
- fix pagination issue on tree view
- fix wrong conversion of string value as boolean

# 3.0.22 (2015-10-22)

## Bug fixes

- fix nested editor rendering issue

# 3.0.21 (2015-10-20)

## Bug fixes

- fix nested editor rendering issue

# 3.0.20 (2015-10-17)

## Bug fixes

- fix dashlet refresh issue
- fix nested editor rendering issue
- fix grid widget rendering issue
- fix onchange issue in editable grid

# 3.0.19 (2015-10-01)

## Bug fixes

- fix onChange issue on grid widget
- fix nested editor value lost issue 
- fix lazy loading dashlet doesn't load data

# 3.0.18 (2015-09-28)

## Bug fixes

- fix form values update issue
- fix dashlet/portlet initialization issue
- fix record update issue
- fix editable grid tab navigation
- fix grid column overflow issue
- fix o2m reload issue
- fix new record issue on editable grid
- fix delayed record loading in popup
- fix popup editor update issue

## Improvements

- improve custom filter management
- improve pending action tracking
- improve action sequence management
- improve loader messages

# 3.0.17 (2015-08-28)

## Bug fixes

- fix selection filter in grid
- fix backspace button as back key
- fix dotted field in editable grid
- fix editable grid cell focus issue
- fix strip indent utility method causing meta loader issue
- fix dashboard/portal initialization issue
- fix toolbar buttons as menu doesn't send _signal
- fix field editor update issue
- fix toolbar buttons adjustment issue
- fix form values update issue

## Improvements

- update javadoc on generated domain classes
- allow all types/classes with finder method params
- improve popup window autosize

# 3.0.16 (2015-08-14)

## Bug fixes

- fix master-detail refresh issue
- fix summary-view issues
- fix onChange in popup editor
- fix dirty checking issue on record save

# 3.0.15 (2015-08-13)

## Bug fixes

- fix pending actions callback issue
- fix nest editor not refreshing record
- fix nested in nested editor not refreshing record

# 3.0.14 (2015-08-12)

## Bug fixes

- fix onChange issue on popup
- fix external editor visibility in grid widget
- fix dotted field update issue
- fix error dialog issues
- fix action-group & action-validate resume issue
- fix duplicate onSelect issue on tabs
- fix various nested editor issues
- fix action execution issue on popup editor
- fix pending action promises are not cleared
- fix wrong validation error from editable grid

## Improvements

- do not allow 'save' with onLoad on unsaved o2m item
- show wait spinner after 5 seconds
- extract i18n strings from xml sources
- always check record version before 'save' action

# 3.0.13 (2015-07-17)

## Bug fixes

- fix save action issues in popup editor (regression)

# 3.0.12 (2015-07-07)

## Bug fixes

- fix duplicate onload issue
- fix dirty state handling with onNew having save
- fix unwanted collapse animation on panels
- fix popup/dialog issue
- fix save action issues in popup editor
- fix collapseIf on panel issue
- fix indexed action execution

## Improvements

- do not allow dragging maximized popup
- allow selector popup not resizable


# 3.0.11 (2015-07-02)

## Bug fixes

- fix onNew issue in calendar popup editor
- fix SelectProgress widget in grid
- fix duplicate read from popup
- fix editors inside panel-stack
- fix editor itemSpan
- fix o2m caching issue
- fix dropdown o2m/m2m in editable grid

## Improvements

- always force "save" action
- add view.form.check-version config option
- add collapse support for panels
- allow expressions with `can` attributes
- add x-show-titles attribute to editor tag
- add refresh button on grid view
- optimize popup editor window

# 3.0.10 (2015-06-19)

## Bug fixes

- fix tree view context issues
- fix x-bind issues (empty value)
- fix form view not in edit mode when opened from dashlet
- fix performance issue caused by distinct select

## Improvements

- lazy loading dashlet/portlet
- support for setting content id from main builder api
- support for buttons in tree view
- allow changing panel title with action-attrs
- don't show search option in m2o dropdown if all items are fetched

# 3.0.9 (2015-06-10)

## Bug fixes

- fix action handler issue (typo)
- fix search view overriding issue
- fix onChange event on toggle widget

## Improvements

- allow resizing popup editor on desktop
- use full width for dashboard on big screens
- don't include context details in i18n catalogs

# 3.0.8 (2015-06-01)

## Bug Fixes

- fix permission check for users without any group
- fix button widget readonly issue
- fix missing data demo value.
- fix action-attrs issue from editor fields
- fix schema definition
- fix button disable status issue
- fix transaction issue on exception
- fix some french translations
- fix print screen issues
- fix onload action issues
- fix mappedBy annotation

# 3.0.7 (2015-04-30)

## Bug Fixes

- fix wrong email sent date issue
- fix file download issue
- fix binary widget download issue
- fix onChange issues on o2m fields
- fix recursion issue caused by an old fix

# 3.0.6 (2015-04-16)

## Bug Fixes

- fixed top-level editable grid save issue
- fixed editable grid update issue (IE only)
- fixed a regression (wrong change detection)
- fixed progress bar widget (IE)
- fixed advance search on o2m/m2m fields
- fixed decimal field issue
- fixed unwrapped viewer template issue
- fixed selection-in on integer selection
- fixed textarea height issue
- fixed text widget issue in editable grid
- fixed grid widget cell formatting issue
- fixed hilite/css issue on text field
- fixed action-method conditional call issue
- fixed nested record update issue
- fixed dirty checking issue on html widget
- fixed parent context issue

## Improvements

- add special client side action 'validate'
- don't show m2o link if canView is false
- add some helper methods to ActionResponse
- hide text field on click outside the grid field
- throw exception if join field not found
- allow decimal scale/precision change on view
- allow password change from user preferences
- don't create demo user from data-init
- prevent certain operations on admin user/group



# 3.0.5 (2015-03-24)

## Bug Fixes

- fixed dependency order issue
- fixed editor context issue
- fixed wait for actions before onNew
- fixed response.view along with response.reload
- fixed meta scanner (search xml files only)
- fixed module loader clean up issues
- fixed duplicate views were not imported properly
- fixed advance search issue on datetime field
- fixed context issue in tree view
- fixed context issue on portlet
- fixed selection issue in tree
- fixed onChange issue on view popup
- fixed I18nLoader encoding
- fixed onNew issue on closed popup editor
- fixed sequence when create records from the web application views
- fixed version check issue
- fixed multiline widget display in grid
- fixed html widget in grid : force multiline editor
- fixed image widget update issue
- fixed month format issue in chart
- fixed color issues in charts
- fixed build.gradle template for app stub
- fixed onClick handler issue when action has prompt
- fixed TagSelect widget issues
- fixed technical info popup
- fixed NavSelect rendering issue
- fixed	integer field handling with selection widget
- fixed SuggestBox canNew and x-create behavior
- fixed data-init issue
- fixed button in toolbar doesn't update title
- fixed duplicate records in search result
- fixed permission check on relational fields
- fixed file selection issue
- fixed wrong model name in help popover
- fixed m2o editor icons issue
- fixed undefined value error in viewer
- fixed attachment list not showing all records
- fixed grid portlet
- fixed wkf missing param
- fixed SmtpAccount NPE

## Improvements

- prevent double click on button
- don't allow init param from extending entity
- support onNew on o2m/m2o editor
- don't allow number width on panel forms
- improved editable grid
- support onNew support to grid widget
- add onNew support on panel-related
- disable hot keys on view popup
- minor code refactoring
- enable mass update button by default
- harmonize application default values
- update translation
- support for custom translation files
- support prompt message on item menu bar
- support for non-jta datasource
- reset form view scrolling
- show validation error for invalid grid fields
- add support for disable auto suggest
- remove hover effect from image button
- add missing title in MetaFile form view
- upgrade TinyMCE to 4.0.1

# 3.0.4 (2015-01-19)

## Bug Fixes

- fixed onSave/onChange issue
- fixed default values issue on editable grid
- fixed selection change issue on grid
- fixed exported file download issue
- fixed version info under eclipse
- fixed ImageSelect issue
- fixed m2o not showing name value
- fixed active cell not editing in editable grid
- fixed successive actions not updating context
- fixed pending action issue on reload request
- fixed inline o2m
- fixed permission issue
- fixed field editor validation issue
- fixed demo data
- fixed dashlet filter issue
- fixed image widget rendering issue
- fixed i18n extract issues
- fixed extra-code/extra-import merge issue
- fixed finder method generation issue
- fixed tree view empty parent node issue
- fixed route issue on record duplicate
- fixed editable grid cell value update and persistent row issues
- fixed cursor x-bind issue
- fixed missing import statements issue
- fixed generic Model.equals test
- fixed automatic bi-direction association setup
- fixed print media css
- fixed child count in tree view
- fixed detached entity passed to persist
- fixed missing audit fields in context

## Improvements

- always generate code for extended entities
- update nvd3
- support for ADK version requirement
- document gradle tasks
- remove static groovy expression compilation
- improve m2o/m2m editor rendering
- allow reload from tab
- allow method as chart dataset provider
- allow prompt attribute from action-attrs
- improved help popover
- add option to save record from view popup
- add option to disable editor on o2m/m2m
- allow expr in search engine.
- update translation
- move 'selection' field definition into object definition.
- allow private setters in bean mapper

# 3.0.3 (2014-11-12)

## Bug Fixes

- fixed a regression that prevented duplicate view finding
- fixed menu user groups on view reset
- fixed menu rendering issue
- fixed i18n extract
- fixed RefSelect dummy widget issue
- fixed duplicate records in nested o2m
- fixed non-owning bi-directional o2o field issue
- fixed empty grid with canNew=false allows new record
- fixed image widget upload issue
- fixed binary widget download/update issue
- fixed tree view column title overflow
- fixed route issue on form view
- fixed code editor empty issue
- fixed unnecessary dirty state from popup
- fixed missing attributes on panel-related
- fixed wrong context on m2o editor
- fixed o2m/m2m resize issue
- fixed help tooltip on prod mode
- fixed create default views

## Improvements

- add system information page
- add active user list
- allow i18n shell command per module
- provide default 'from' address for smtp server
- add replayTo to mail build
- allow title on editor fields
- show truncated button text as tooltip
- remove trailing comma in array and object literals
- allow editor as viewer
- improve placeholder style in form views
- improve MetaModel view

# 3.0.2 (2014-10-14)

Bug fix release.

## Bug Fixes

- fixed gradlew.bat not deploying axelor-gradle (windows)
- fixed dropdown menu z-index issue
- fixed o2o formatting issue (grid view)
- fixed o2m/m2m grid resize issue
- fixed editable grid related issues

## Improvements

- support i18n extract for js/html sources
- csv based i18n support for web client
- support for installing modules by default (RM-2576)

# 3.0.1 (2014-10-08)

Bug fix release.

## Bug Fixes

- fixed some tree view related issues
- fixed F2/F3 permission issue
- fixed pending action issue for method actions
- fixed editor field selection
- fixed module install/uninstall issues
- fixed action-condition errors issue
- fixed MetaFiles.getPath returns wrong path
- fixed image widget height issue
- fixed print.css
- fixed error & login dialogs (make responsive)
- fixed height issues on various widgets
- fixed rollbackOnly transaction in xml import (catch the root cause)

# Improvements

- make button width 100%
- re-designed about page
- removed obsolete code
- do not attach panels in form view

# 3.0.0 (2014-09-23)

Fully responsive mobile ready views, gradle based build system and much more.

## Features

- migrated to gradle build system
- axelor cli utility (command line tool)
- fully responsive mobile ready views
- new responsive dashboard view (replaces portal view)
- custom editor/viewer support for new responsive form view
- removed nested editor support (use custom editor feature)
- implemented repository pattern
- generate pojo only domain class
- new customizable login page

# 2.1.0 (2014-06-27)

New translation system, security api, templates and mail support.

## Features

- re-implementation of security api
- custom sequence generator
- support for custom guice module to configure services
- new reflection api to search for resource in java classpath
- new mail send/recieve mails
- new template api (with multiple template engine support)
- integrated quartz scheduler
- on-the-fly record creation for m2o/m2m widgets
- implemented notification popup
- re-implemented code editor using CodeMirror widget
- support for external application.properties
- re-implemented translation support
- implemented translation extraction utilities

# 2.0.0 (2014-01-15)

Complete re-write of web client and major changes on server framework.

## Features

- angular.js + bootstrap + jquery based web ui
- inline editable grid widget (SlickGrid)
- major refactoring of data import api
- major refactoring of data persistence api
- workflow & bpmn support
- native sql formula field support on domain objects
- generate collection field helpers in domain class
- generate equals, hashCode and toString methods for domain class
- generate finder methods in domain class
- support for archived records
- support for hubernate L2 cache (ehcache)
- action-group to execute actions in a group
- binary & image field support
- csv based translation support
- support for document management
- optimized groovy script evaluation
- implemented readonly form view support
- implemented calendar view
- implemented chart view
- implemented tree view
- dynamic widget attributes
- embedded portlet widget for form view
- MultiSelect & TagSelect widgets
- RadioSelect & NavSelect widgets
- RefSelect widget (two part reference selection)
- module install/uninstall support
- view override support (duplicate views with same name)
- implemented html widget (TinyMCE)
- implemented advance search with custom & saved filters
- implemented mass update feature
- domain object inheritance & aggregation support
- grid row/cell highlight support
- keyboard shortcuts

# 1.0.0 (2013-07-17)

First stable release

## Features

- xml domain object definitions
- xml view definitions
- xml actions
- xml data import
- csv data import
- simple security api
- smartclient based web ui
- hibernate + guice + tomcat

# 0.9.x (2012-2013)

Initial prototype releases
