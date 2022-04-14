```
                          ggg@@@$$@@Bggg;
                      gg@$$$$$$$$$$$$$$l$&$@gg,
                   ;g@$$$$$$$$$$$$$$$$$$$$PF  ]7Bg,
                 y@$$$$&$$$$$$$$$$$$$$@|L`        *Ng,
               ,@$$$$$$$$$$$$$$@B$$$%@@L|        ]Bg,*RWg
              y@$$$$$$$$$$$$@&l$$$$$$$$K|          ]$&, ]7Bg
             $@$$$$$$$$$$$$@W$$$$$$$$$$P`           "$$&   -*Bg
            J@$$$$$$$$$$$$$$$$$$$$$$$@P'             '$@$&    -7Ng
            $@$$$$$$$$$$$$$$$$$$$$$$@L                 $@$$g     ]&g
            @$$$$$$$$$$$$$$$$$$$$$@P                  ]@@@@$$g     +%g
            $@$$$$$$$$$$$$$$$$$$@$|             gg@P ygPPIT$@$&      -%N
           gB@$$$$$$$$$$$$$$$@BT||L          ;@@PP=  +   g$gg$@@       l$@
         g@L|&@$$$$$$$$$$$@@$-|||||  $&g    $@P          ?T%$$$@1$F   g$$$$g
        @$||||2B@$$$$$@@B@NRRNB@@g|l,"$@@g            ;LL>, 2@$@$$F $$$$$$$$@
      ;@&|||||l -]$@&NB@g|      |T|||4@@@P     @     |||$@g||$@Q$$F]$$$$$$$$&@
     y@$|||||L  @P+|ggg|||L,,  ,,|||||,,ggggg@P7    /||g@@PJ@B$$$$F$$$$$$$$$$$K
    )@$F||||L  ]@RP7$@P[ ,|||||||||$@@@@@@@@@@    ,l|$@@@7  @$$$$$W$$$$$$$$$$&@
    @$$||1$@       $P|;l|||||||||||||||N@@@@@    ||g@@@P    @$$$$$$$$$$$$$$$$$@
   $@$$ j$$$ gr  ,$@L||g@@@gg||||||||||||L++  ,gg@@NP-     ]@$$$$$$$$$$$$$$$$$$-
   @$$$ $$$$@$$gg$$@@@P -JN@@@@@@@@@@@@g@g@@@@@NP^         @$&@$$$$$$$$$$$$&$$@
   @$$$$$$$$$$$$$$$@        -]*PPNBBBBNNPPP7+             ]@$$@$$$$$$$$$$$@@$$@
   @$$@$$$$$$$$$$$&@                                      @$@@$$$$$$$$$$$@$@$@P
   $@$B@$$$$$$$@$$$$-                                    ]P[$@$$$$$$$$$@P $@@C
    $&@]$$$$$$$$@$$&@                                       @$$$$$$$@P7   $@P
    7$@  %@$$$$$&@@@$@                                     J@@@@RPZ-
      %P   "R@@@$$$@C*P


      ,,,,,,,, "=[]"  ,,,,,,,        ,,,,      ;;ggg      ,,,,,,      ,,,,,
      $@@@@@@@@@@g   ]@@@@@@@@@@N,   @@@@F  g@@@@@@@@@N   $@@@@@@    $@@@@@@
      $@@@P-~?$@@@P  ]@@@@--+J@@@@   @@@@F J@@@@-  J@@@@  $@@@@@@    @@@@@@@
      $@@@P   $@@@@  ]@@@@    @@@@   @@@@F ]@@@@-   7777  $@@@$@@P  ]@@R@@@@
      $@@@P   @@@@P  ]@@@@;;gg@@@P   @@@@F  ]B@@@@g       $@@@]@@@  @@@]@@@@
      $@@@@@@@@@@P   ]@@@@@@@@@@g    @@@@F    7N@@@@@g    $@@@ $@@ ]@@PJ@@@@
      $@@@P77]+      ]@@@@  +J@@@@   @@@@F       ]&@@@@-  $@@@ ]@@K$@@ J@@@@
      $@@@P          ]@@@@    @@@@   @@@@F $&&&P   ]@@@@  $@@@  @@@@@@ J@@@@
      $@@@P          ]@@@@    @@@@   @@@@F J@@@N, ,@@@@@  $@@@  $@@@@- J@@@@
      $@@@P          ]@@@@    @@@@   @@@@F  ]B@@@@@@@@P   $@@@  -@@@@  J@@@@
                                                               by Yume Labs

```

# Prism Engine

Prism Engine is a next generation asset rendering program for the Solana
blockchain network. It handles the generative creation of metadata and images
for NFTs (non-fungible tokens) from configuration.

Whereas traditional rendering engines expect a 1:0-1 attribute:image layers map,
with Prism each attribute in the metadata can map to:

  - Any number of image layers;
  - Any number of image color tints;
  - Any number of image blending modes;
  - Any number of image transparency changes; AND
  - Any number of image filters.

In addition, Prism supports stopping and resuming generation of NFT collections.
This is useful if the user wishes to generate assets whilst AFK, but not have
the overhead of running the application whilst present at the keyboard.

## Usage

### Configuration

Prism Engine configurations are written in 
[EDN format](https://github.com/edn-format/edn), which is a powerful data
notation native to the Clojure language. It's worth getting a cursory
understanding of the format before starting to write your configuration.

Check out [the docs](./doc) for more information on how to configure the 
application, as well as a tutorial showing how to build a simple collection.

### Running

There are several ways to run Prism Engine. We recommend that new users start
with the pre-built JARs.

#### Pre-Compiled JARs

You'll need to have a modern Java runtime installed on your machine in order to
run from a local JAR file. You can install this from your operating system's
package manager, or from 
[the java.com website](https://java.com/en/download/manual.jsp).

Next, head to our `releases` section on GitHub and grab the version you want
(this will usually be the latest version).

Now, run Prism Engine and follow the on-screen configuration instructions:

```
$ java -jar prism-x.y.z-standalone.jar
```

#### From Source Code

We recommend compiling from sources only for people who want to run cutting-edge
versions of Prism (which may come with unexpected bugs) or for people who wish
to contribute to the development of Prism.

You will need `git`, `leiningen`, `clojure`, and `java` installed to do this.
You can get each of these tools from your operating system's package manager, or
from the official websites:

  - [git](https://git-scm.com/downloads)
  - [clojure](https://clojure.org/guides/getting_started)
  - [leiningen](https://leiningen.org/#install)
  - [java](https://java.com/en/download/manual.jsp)

First, pull the GitHub repository:

```
$ git clone https://github.org/Yume-Labs/prism
```

Next, change to the directory you pulled the repository from and install 
the project dependencies:

```
$ lein install
```

Optionally, run the tests:

```
$ lein test
```

Then compile an uberjar:

```
$ lein uberjar
```

Now, run Prism Engine and follow the on-screen configuration instructions:

```
$ java -jar target/uberjar/prism-x.y.z-SNAPSHOT-standalone.jar
```

### What Next?

Once you've finished generating assets, manually sanity check them to ensure
that the images & metadata were generated correctly. They're in the expected
format for Metaplex Candy Machine, so you can follow the instructions on the
[Metaplex website](https://docs.metaplex.com/candy-machine-v2/getting-started).

Skip the section on how to generate assets, though you may wish to verify the
assets as normal to ensure that your chosen filetypes, character sets, etc are
supported (Prism does not perform these checks).

## Support

Yume Labs provides dedicated support to holders of the following NFT 
collection families:

  - The Glitterflies
  - Kitten Coup
  - Folktales of Lunaria

To receive dedicated support, please verify your ownership of one of these
collections in [our Discord server](https://discord.gg/yume-labs) and ask in the
dedicated Prism support channel.

If you are not a Yume Labs holder, we only offer support for bugs in the
software. Please open a ticket here on GitHub.

## Contributing

If you wish to contribute to Prism, please fork the repository and submit a pull
request to the repository with your changes and a justification for those
changes. We welcome contributions from outside of Yume Labs.

## Todo

  - Improve documentation;
  - Provide a Docker-based method to run Prism Engine;
  - Add support for Ethereum-based NFTs.

## Credits

  - [MistyBayou](https://twitter.com/mistybayounft) - Source code & documentation
  - [Inktolin](https://twitter.com/inktolin) - ASCII art

## License

```
Prism Engine - A Tool for Generative Art & Assets
Copyright (C) 2022 Yume Labs

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see 
[https://www.gnu.org/licenses/](https://www.gnu.org/licenses/).
```
