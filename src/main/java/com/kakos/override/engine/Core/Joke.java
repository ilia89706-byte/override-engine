package com.kakos.override.engine.Core;

import com.kakos.override.editor.Console;

public class Joke {
    public static void printJoke(){
        String[] funnyPhrases = {
        "Hello, overrider. Who did you expect? Elon Musk?",
        "Booting up... Please do not feed the bugs after midnight.",
        "Compiling dreams, executing bugs.",
        "Override is loaded. Time to write some 'temporary' code that lasts forever.",
        "Hello. If anything breaks, blame the Lua scripts.",
        "Console initialized. Ready to ignore your warnings.",
        "Welcome back. May the frame rate be with you.",
        "Everything is fine. Except for the parts that aren't.",
        "System ready. Let's turn coffee into pixel art.",
        "Engines on. Remember: it's not a bug, it's an undocumented feature.",
        "Talk is cheap. Show me the code. — Linus Torvalds",
        "Measuring programming progress by lines of code is like measuring aircraft building progress by weight. — Bill Gates",
        "If debugging is the process of removing bugs, then programming must be the process of putting them in. — Edsger Dijkstra",
        "Always code as if the guy who ends up maintaining your code will be a violent psychopath who knows where you live. — John Woods",
        "There are two ways to write error-free programs; only the third one works. — Alan J. Perlis",
        "It works on my machine, but we are deploying to your machine today.",
        "There are 10 types of people in the world: those who understand binary, and those who don't.",
        "Again and again, and once more...",
        "I'm ready to digest the prints.",
        "A scene walks into a bar, and—without being asked—it gets thrown out to the audience.",
        "No one knows how it works.",
        "The hardest part of development is catching the moment of \"anticipation\" and holding onto it.",
        "No, no, there’s no gag this time—just the console, just waiting for the print.",
        "Pepperoni and green peppers, mushrooms, olives, chives!",
        "If you don't like what I'm saying, don't buy my f###g CD.",
        "Boom-na-da-noom-na-nam-ena! Twist! Twist!",
        "It's a Teto Takeover! Everyone, follow me!",
        "Fate is like a painting—paint it the way you want to see it. - Krab",
        "https://youtu.be/G1lQqIcUDEI?si=7Dw-c8cWd_v4B9Ig",
        "Honestly, I’m tired of coming up with catchphrases; I’ll just add this, you know? Yeah.",
        "A.D.I.D.A.$",
        "Did My Time.",
        """

                        .-------------------.
                       /   ________________  \\
                      /   /                \\  \\
                 ____/___/__________________\\__\\___
                /                                   \\
               |                                      |
               |       /\\                   /\\       |
               |      /  \\                 /  \\      |
               |                                     |
               |        _______________________      |
               |                                     |
                \\                                   /
                 \\_________________________________/
                 Buy me pls.
""",
"""                                                                                                                                        
                                                                            .... ..  . . ...        
                                                                           .-. ...... .:=..         
                                                                      ..:   ..       .    ....      
                                                                    .. :  . . . . ..-==. ...:       
                                                                    .   . .-=. .:-.. :==.... -.     
                                                                     -.-.=:.:--. ....  ..  ..-      
                                                                     = .. . . .   .:.  ......:      
      .. =.     :=. ...                                             .- . .-==-.  .. .         .     
   ..-               -.  . .                                        . .. .  .=-  -.==@@..   . -     
.. =...              ...: ..                                          . ...@@....=..    .   ..-     
...                      :.   .                         ..  . ..      -.       - ...         .-     
. ..                       .  .                        . .=-=+- .   . .:      :.  .-.       ..:     
. .  ......:-=--. ...  .... ...                           .+.... -+. . :   ...=.   -.    .. .+-   . 
. :-..:=-:...-...:-=:..-=.:=..=                           . .  ..  ... .=.    .... .      .=-:*=.   
.:. .::  .. ..   ....  . -. .  ...                        ... =         .* ..:.:%#%* :.:.--:-=-.. ..
 .. .  . ..  ..  ... ..=-= .   :.                             . + ..  .. ..--: @@@@@@:.::. #=.:     
..=-==-.    ..   ..  .  . ..   =.                              .  .-..   . .*=.:@@@@+    =+-...     
.. -... .= .:=  .::..-=--. .+--+:.                                ...=       -:.... .  .=:..        
. .:.@@@.+. .=-=*=.. %@=.  .  ==-.                               .  ..=.   .. === .. ..=.. .        
  .::    = .-=...==..-= ..:.  *-..                                   .. ..    . . +. --             
 -=.... .-++.=    *.+.. . :+:= .                                         .= .                       
. ....... .. .   . . .     ..... .                                      . ..                        
           .=.. .  =        .... .                                         ..                       
            =.. ..+.        . .                                            .:                       
           ...  .....       ..=...                                         ...                      
           .- .:.=. .       .-=...                                          .:                      
. ..   ... . :---.+..       =+.                                             ..                      
. ..   . =. .@@@#..:-... ...+=.                                              .. ..                  
....   .: ==@@@@@@.=:. ..:+ -..                                            .. =                     
..... . -.=.@@@@@@. . :.-==+- .                                               ..                    
=. ....:-- -.@@@@.-  =..:+-- .                                                 :..                  
=---.::..-- .. .  =..:..**:.                                                  .=..                  
.#-.=.-.. =..::....:.:.+: ..                                                  .= .                  
= -=::::. -..- ... *-:::...                                                   .- .                  
-.... -+.-- ....::.-+:. ....                                                   -.                   
-......-.+=:-+=.=*=:. ... =.                                                  :...                  
        """
};

String randomPhrase = funnyPhrases[(int) (Math.random() * funnyPhrases.length)];
Console.log(Console.Modifier.OVERRIDE, randomPhrase);
    }
}
