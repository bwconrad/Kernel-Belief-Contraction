# EL++ Belief Contraction 
A system for removing axioms from EL++ ontologies using the kernel contraction
algorithm and a total preorder rule to minimize information loss. A detailed explaination of the
algorithms used can be found
[here](https://bwconrad.github.io/2019/07/05/belief-contractions-on-large-ontologies-with-minimal-knowledge-loss.html). 

## Installation
This is a Maven project that was developed in Eclipse. Pom file contains all dependencies 
that need to be installed. 

## Usage
Run 'beliefContraction' using the program arguments described below:
+ **Arguments**: <inputFile> <outputFile> <belief> <method> (ex. input.owl output.owl "A SubClass B" -s)
    + __inputFile__: An OWL ontology file. All axioms not allowed in OWL-EL
      are automatically removed. 
    + __outputFile__: File name for contracted ontology.
    + __belief__: The belief that you wish to contract. Written in [Manchester
      syntax](https://www.w3.org/TR/owl2-manchester-syntax/#Formal_Description_for_Mapping_to_OWL_2_Functional-Style_Syntax).
    + __method__: The aglorithm used to select the drop set.
        + -s: chooses the most specific hitting set
        + -n: chooses any min hitting set

The 'test-ontologies' directory contains sample OWL ontologies that can be used as input. 

## Examples

1) **Arguments**: ../test-ontologies/small-1.owl ../out.owl "A SubClassOf G" -s
> Belief: SubClassOf(\<A>\<D>)


> Kernels: 
> 1) EquivalentClasses(\<C>\<D>) SubClassOf(\<A>\<B>) SubClassOf(\<B>\<C>)

> Dropping: SubClassOf(\<B>\<C>)

2) **Arguments**:  ../test-ontologies/eqiv.owl ../out.owl "A SubClassOf G" -s
> Belief: SubClassOf(\<A>\<G>)

> Kernels:  
> 1) SubClassOf(\<A>\<B>) EquivalentClasses(\<D> ObjectSomeValuesFrom(\<r>\<C>))
> SubClassOf(\<D>\<E>) EquivalentClasses(\<E>\<G>) EquivalentClasses(\<B>
> ObjectSomeValuesFrom(\<r>\<C>)) 
> 2) EquivalentClasses(\<A>\<F>) SubClassOf(ObjectSomeValuesFrom(\<r>\<D>)\<G>) SubClassOf(\<F> ObjectSomeValuesFrom(\<r>\<D>))

> Hitting Sets:
> 1) SubClassOf(\<A>\<B>) EquivalentClasses(\<A>\<F>)
> 2) SubClassOf(\<A>\<B>) SubClassOf(\<F> ObjectSomeValuesFrom(\<r>\<D>))

> Multiple minimum hitting sets. \
> Please choose which set to remove: 
1

> Dropping: 
SubClassOf(\<A>\<B>) EquivalentClasses(\<A>\<F> )

