
# Development Idea - Voice based coding

This document describes the idea for creating a voice-based
input-conversion program.

## Vision

Using Kotlin has been a wonderful coding experience.
I am fluent in Kotlin, in such a way that I can speak my code faster,
than I can type it.

I think that a program that converts what I say into code is possible.
I have first-hand experience with all components that are necessary. 

I know they work, and they are reliable.

I might not be the first to think of this, 
but I have all the tools that I need. Right now.


### Example

Imagine me, sitting at my laptop.

My text editor is Intelli IDEA and it has focus.

I say:

<code>drawer dot for_each lambda enter x is 5 leave</code>

Those recognized language tokens called "terminals" will get mapped to virtual keyboard inputs:

<code>drawer . forEach { ENTER x = 5 KEY_DOWN }</code>

The input commands are executed and give the following result:

<code>1:<br>
2: drawer.forEach {<br>
3:     x = 5 <br>
4: }<br>
5:</code>

### Components

* Voice Recognition via Desktop DialogEngine
* Key strokes via [some software for live key strokes]
* IntelliJ IDEA with code completion
* (for this example:) OPENRNDR GitHub template

### Executable Example

I created a functioning example. It is documented with JavaDocs, explaining
its (fictional) creation.
That example is executable if you have the setup described in the docs.

## Product

Speech-to-Text software specifically for coding
