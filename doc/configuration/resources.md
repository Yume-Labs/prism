# `:resources`

The `:resources` section describes every resource which will be used by the
rendering engine, primarily these are images or colors.

## Sample

```clojure
{:resources {:images {:body "doc/tutorial/res/body/body.png"
                      :eyes {:open "doc/tutorial/res/eyes/open.png"}}
             :colors {:cream [255 241 237]}}}
```

## Images

Images are generally represented by their path relative to the root of the
application.

## Colors

Colors are represented in RGB format as a list [R G B], where each of R, G, and
B are a number between 0 and 255.
