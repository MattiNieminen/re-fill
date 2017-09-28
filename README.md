# Re-fill

A collection of Re-frame components that most applications need. Currently,
Re-fill offers event handlers, subscriptions, effects and co-effects for:

* Navigating and dispatching between routes and rendering their corresponding
views.
* Global notifications, which can be hidden after timeout (just bring your
own views).
* Debouncing events (making sure that only one event of certain type is
scheduled to be ran at once.)
* Generating UUIDs to your event handlers.

## Usage

Re-fill contains multiple utilities, which all have their own namespaces.
It's possible to use only one of the utilities, or any combination of them.
As in Re-frame, event handlers, subscriptions etc. will be registered
globally after requiring the specific namespace for the wanted utility.

### Routing

Routing utility provides means to listen for URL changes and dispatch events
based on them, making it possible to write bookmarkable single-page apps
with ease.

Currently, Re-fill only supports routes defined using
[Bidi](https://github.com/juxt/bidi) syntax. For listening the URL in
your address bar, Re-fill uses [Pushy](https://github.com/kibu-australia/pushy).

To use Re-fill for routing, first require the correct namespace(s):

```clj
(ns example.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-fill.routing :as routing]))
```

Then, define your routes using Bidi syntax (no need to add dependency for it
though). For example:

```clj
(def routes ["/" {"" :routes/home
                  ["page/" :id] :routes/page}])
```

It's recommended to use namespaced keywords, as those keywords will be
dispatched by Re-frame.

You also need to create mappings between routes and views:

```clj
(def views {:routes/home home-view
            :routes/page page-view
            ;; Value of :else will be used if there's no mapping for route
            :else loading-view})
```

In order to render the correct view, add ```routed-view``` in your root view
/ template, and give the mappings from previous step to it as argument.
Here's an example:

```clj
(defn main-view
  [views]
  [:div.main
   [:h1 "This is shown in all views"]
   ;; views refers to the mappings created in the previous step
   [routing/routed-view views]])
```

Lastly, in order to initialize the routing, dispatch
´´´:re-fill/init-routing``` during the startup of the application:

```clj
(defn init!
  []
  ;; routes refer to your Bidi-routes
  (rf/dispatch [:re-fill/init-routing routes])
  ;; views refer to your route - view mappings
  (r/render [main-view views]
            (js/document.getElementById "app")))

(init!)
```

After doing this, the URL in the address bar will be listened by Re-fill
and the corresponding view will be rendered by template. You can either use
links normally with hrefs (no preventDefault or manual history manipulation
is needed), or dispatch ```:re-fill/navigate``` event to navigate
programmatically. Here's an example of both:

```clj
[:div
 ;; Normal navigation with link
 [:a.controls__a {:href "/page/1"} "Page 1 (by link)"]
 
 ;; Navigation by dispatching an event
 [:button.controls__button
  {:on-click (fn [_]
               (rf/dispatch [:re-fill/navigate
                             ;; The argument to re-fill/navigate is the actual
                             ;; route with path-params.
                             ;; See Bidi documentation for more information
                             [:routes/page :id 2]]))}
  "Page 2 (by dispatch)"]]
```

The event ```:re-fill/navigate``` is great for initiating navigation from views.
There's also an **effect** handler for :re-fill/navigate, which makes it
possible to navigate from event handlers without dispatching more events.
This is useful for cases where the view dispatches an event, and the event
handler may conditionally initiate navigation to some other view in addition
to other side effects (through effects of course).

If you need to refer to your current route from your views, you can use
```:re-fill/routing``` subscription for that:

```clj
(defn page-view
  []
  (let [routing @(rf/subscribe [:re-fill/routing])]
    [:h1 (pr-str routing)]))
```

After navigation happens, Re-fill dispatches an event using the route key as
an identifier for it. It's recommended to register an event handler for each
of the routes, otherwise you get a warning to console about a missing event
handler. This dispatched event is great for setting up the state for the view
that's gonna be rendered. Here's a simple example:

```clj
(rf/reg-event-fx
 :routes/home
 ;; The first argument is the co-effects (normal Re-frame stuff)
 ;; The second argument is the event itself. The route match
 ;; from bidi can be destructured from it
 (fn [_ [_ bidi-match]]
   ;; In real apps, this function would return effects map for
   ;; fetching data, setting state or something else.
   (js/console.log "Navigated to " bidi-match)))
```

See
[full example](https://github.com/metosin/re-fill/tree/master/example-src/example/core.cljs)
for more information.

### Notifications

Notifications utility provides a generic notification interface for
creating and deleting notifications, and a subscription for using them
in your views. Notifications are often used to show information or warning
dialogs to end-users.

In order to use notifications, the correct namespaces must be required:

```clj
(ns example.core
  (:require [re-frame.core :as rf]
            [re-fill.notifications :as notifications]))
```

After that, notifications can be created with ```:re-fill/notify``` event.
For example:

```clj
[:button.controls__button
 {:on-click (fn [_] (rf/dispatch [:re-fill/notify
                                   ;; The first argument is the data
                                   ;; you want to use as notification.
                                   ;; It's your responsibility to add types
                                   ;; your notifications if you need them.
                                   {:type :success
                                    :content "Success!"}
                                   ;; The second (optional) argument is used
                                   ;; by re-fill to configure the notification.
                                   ;; Currently, the only supported key is
                                   ;; :hide-after, which can be used to delete
                                   ;; the notification after a given time (in
                                   ;; ms) has passed.
                                   {:hide-after 3000}]))}
 "Notify success!"]
```

If ```:hide-after``` is used, the notification will be removed automatically
after the timeout has passed. In order to delete the notification manually,
the ```:re-fill/delete-notification``` event can be used. 

A view must be created for rendering notifications. Re-fill provides a
subscription ```:re-fill/notifications``` for getting the notifications
in view functions.

Here's a simple view to demonstrate the usage of
```:re-fill/delete-notification``` event and ```:re-fill/notifications```
subscription:

```clj
(defn notifications-view
  []
  ;; Subscription to notifications
  (let [notifications @(rf/subscribe [:re-fill/notifications])]
    [:div.notifications
     (for [{:keys [id type content]} notifications]
       [:div.notification
        {:key id
         :class (case type
                  :success "notification--success"
                  :warning "notification--warning")}
        [:span.notification__span content]
        [:button.notification__button
         ;; Deleting a notification requires the id of notification
         ;; Unique id is generated automatically by Re-fill 
         {:on-click #(rf/dispatch [:re-fill/delete-notification id])}
         "x"]])]))
```

See
[full example](https://github.com/metosin/re-fill/tree/master/example-src/example/core.cljs)
for more information.

### Debounce

Debounce utility can be used for:
* Scheduling a dispatch of some event to a later time
* Canceling those scheduled events before they are dispatched
* Identifying scheduled events with keys to make sure that an event of certain
type has been only scheduled once.

For those who are new to debounce there's a good explanation of debounce at
[css-tricks.com](https://css-tricks.com/debouncing-throttling-explained-examples/)

To use debounce utility, the correct namespaces must be required:

```clj
(ns example.core
  (:require [re-frame.core :as rf]
            [re-fill.debounce :as debounce]))
```

To schedule an event to the future, the event ```:re-fill/debounce``` can be
used. Here's an example of how to schedule the ```:re-fill/notify``` with
a one second timeout.

```clj
[:button.controls__button
 {:on-click (fn [_] (rf/dispatch [:re-fill/debounce
                                  ;; The :key is used to identify the scheduled
                                  ;; event.
                                  {:key :test-notify
                                   ;; The actual event that will be dispatched
                                   ;; or debounced.
                                   :event [:re-fill/notify
                                           {:type :success
                                            :content "From debounce!"}
                                           {:hide-after 3000}]
                                   ;; Timeout in ms for scheduling the dispatch
                                   ;; to the future.
                                   :timeout 1000}]))}
      "Notify with debounce"]
```

Debounce is more than just ```:dispatch-later``` built into Re-frame: this
scheduled event can be moved further into the future by dispatching
```:re-fill/debounce``` again with the same ```:key``` that was used before.
This means that in the example above the ```:re-fill/notify``` event won't be
dispatched ever if the user keeps clicking the button repeatedly with the less
than one second intervals.

Debounce is great for dispatching events after the user has stopped doing
something that results in multiple browser events, such as starting a search
after the user has stopped typing.

There's also a subscription ```:re-fill/debounce``` for getting all the
scheduled events which have not yet been dispatched. Debounce utility also
allows canceling the dispatch of the event while the timeout is still active
by using the ```:re-fill/stop-debounce``` event.

Here's an example of a cancel button which is only clickable if the event has
scheduled

```clj
(defn cancel-button []
  (let [debounce @(rf/subscribe [:re-fill/debounce])]
    [:button.controls__button
     {:on-click (fn [_] (rf/dispatch [:re-fill/stop-debounce :test-notify]))
      :disabled (not (:test-notify debounce))}
     "Stop debounce"]))
```

See
[full example](https://github.com/metosin/re-fill/tree/master/example-src/example/core.cljs)
for more information.

### UUIDs

Re-fill provides a co-effect ```:re-fill/uuids``` for injecting UUIDs to
event handlers. In fact, the ```:re-fill/notify``` event handler uses
it internally to generate unique ids for each notification.

```:re-fill/uuids``` takes a single argument: the amount of UUIDs
to be created. Here's an example of how to use it:

```clj
;; re-fill.uuid must be required

(rf/reg-event-fx
 :your/event
 [(rf/inject-cofx :re-fill/uuids 2)]
 ;; :uuids can be destructured from co-effects
 (fn [{:keys [db uuids]} _]
   ;; uuids is a vector of 2 UUIDs
   ))
```

Here's how
[re-fill.notifications](https://github.com/metosin/re-fill/tree/master/src/re_fill/notifications.cljs)
utilizes this.

## Development

All the files for actual library are located under
[src](https://github.com/metosin/re-fill/tree/master/src/re_fill)
directory.
There's an example app for development and testing under
[example-src](https://github.com/metosin/re-fill/tree/master/example-src/example)
and
[example-resources](https://github.com/metosin/re-fill/tree/master/example-resources/public)
directories.

To get an interactive development environment run:

    lein figwheel

and your browser will open automatically at
[localhost:3449](http://localhost:3449/). From there, you get the
normal Figwheel development flow.

## Releasing

```bash
lein deploy clojars
```

## TODO

* Migrate from Bidi to Reitit.
* Add documentation about complementing libraries
 * A Re-frame library for making HTTP requests is the most important
* Study re-frisk, or implement a custom dev component for viewing
state and time traveling between events.
* Study how to (optionally!) use hash / URL fragment instead of
History API.
* Consider re-naming the debounce related event handlers, effect handlers
and subscriptions to something more suitable.
* Figure out a better example app.
* Figure out how to keep code in README and example app in sync.

## License

Copyright © 2017 Metosin

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
