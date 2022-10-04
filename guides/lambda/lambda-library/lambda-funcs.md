---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Descriptive Statistic & Basic Maths functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## FUNCS

### About

Array of functions.

Allows for creation of an array of functions which can be passed as a parameter to another function.

e.g. = TRANSFORM(vector, FUNCS(SQRT, LN, LOG_10))

### Code

{% capture code %}
FUNCS = LAMBDA(fn_1, [fn_2], [fn_3], [fn_4], [fn_5], [fn_6], [fn_7], [fn_8], [fn_9], [fn_10],
    LET(
        //An array indicating which functions are omitted
        omitted_fns, VSTACK(
            ISOMITTED(fn_1),
            ISOMITTED(fn_2),
            ISOMITTED(fn_3),
            ISOMITTED(fn_4),
            ISOMITTED(fn_5),
            ISOMITTED(fn_6),
            ISOMITTED(fn_7),
            ISOMITTED(fn_8),
            ISOMITTED(fn_9),
            ISOMITTED(fn_10)
        ),
        //count of the not omitted functions
        fn_ct, SUM(--NOT(omitted_fns)),
        //return the first fn_ct functions in an array
        fns, CHOOSE(SEQUENCE(fn_ct), fn_1, fn_2, fn_3, fn_4, fn_5, fn_6, fn_7, fn_8, fn_9, fn_10),
        fns
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}