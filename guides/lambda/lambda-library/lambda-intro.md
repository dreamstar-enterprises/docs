---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Introduction to Lambda
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## Introduction

Lambda is a relatively new function to Microsoft Excel (introduced in 2021) that now allows users, for the first time in over 30 years, to create their own bespoke functions (including recursions). 

More generally, in computer programming, the term 'Lambda' refers to an anonymous function - something you can use you to write functions quickly without naming them, or even to create functions from functions.

The concept of using lambdas in functional programming is not really new, (it's been around for decades), but for spreadsheet users, it is new, and when it was introduced into the Excel ecosystem, it had, for very good reason, stirred up a lot of interest and excitement! 

The purpose of this guide is to serve as a curation of really useful lambdas (that I have found from across the world wide web). If you follow the 'Mr Excel lambda' forum, one of the users there is really pushing the limits of what is possible with Lambdas in Excel (I had to really think hard to understand the mechanics of these calculations), so much of the credit to these functions actually goes to him (or her?). I am mostly the orgainser here.

I've included a reference to the relevant website link on the page to each function I have also tested each lambda, to make sure that they work as expected, and done a first pass review to make sure there is nothing funny in any of them, that shouldn't be there.

## Library

There is A LOT MORE you can now do with lambdas - things that were were either not possible to do before (e.g. recursions), or which literally took several hours to do (array transformations).

So they are very powerful!

As you will gradually realise, there are a lot of ways you can write a lambda to perform the same calculation or task. Some may be kinder to Excel's internal calculation engine, others less so. However, understanding Excel processor optimisation is not what this guide is about. With improvements being made all the time, even the most complex of lambdas should take only a few hundred milliseconds to complete, so it's not something one should really worry too much about. 

And if you don't prefer real-time calculation or transformation updates in your spreadsheets, then, of course, you can always use ETLs and Power Query, or even the new Office Scripts (Java Script).

I've tried to group the functions I've found into clear and logical categories. So far, by: 

* Combinatronic functions
* Data & Time functions
* Array Transformation functions
* Array 'By Element' functions
* Descriptive Statistic & Basic Maths functions
* Finance & Accounting functions


...though, I'm sure, with time, the taxonomy will evolve and change.

## Rest of this Guide

If you'd like to find out and learn more, then please read ahead!


