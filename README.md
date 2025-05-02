[![Build Status](https://travis-ci.org/openmrs/openmrs-module-emrapi.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-module-emrapi)

openmrs-module-emrapi
====================

Higher-level APIs to support building EMR functionality in OpenMRS, to supplement the data-level APIs in the OpenMRS core.

# Dependencies

## Required core version

* 2.2.1

## Required modules

* event
* metadatamapping
* webservices.rest

## Aware of modules

* fhir2
* metadatasharing
* providermanagement
* reporting

# Configuration

* EmrApiProperties + EmrApiConstants wraps much of the configuration, much of which is in GPs and metadata mappings

# Startup

* Creates a role named "Privilege Level: Full", if it doesn't exist, and ensures it has every privilege that does not start with "App:" or "Task:"
* Creates a role named "Privilege Level: High", if it doesn't exist, and ensures it has every privilege that does not start with "App:" or "Task:" and is not in a list of "unsafe" privileges
* Creates metadata source, metadata mappings, and metadata sets that are required for the configuration
* Creates an "Unknown Provider" provider if it does not exist
* Sets default visit assignment handler via global property
* Sets up the patient viewed event listener subscription
* Ensures the person image folder exists

# REST API
See https://github.com/openmrs/openmrs-contrib-rest-api-docs

# Functionality

## Inpatient care, ADT, and dispositions

The emrapi module provides an API for defining patient dispositions and ADT workflows and tracking.
In order to configure one's system to utilize the EMR API functionality as expected, the following is required:

### Admissions, Discharges, and Transfers

All ADT events are modeled as encounters of specific types.  These must be configured using the metadata mapping module as follows:

| Mapping Source             | Mapping Code                            | Metadata Class            |
|----------------------------|-----------------------------------------|---------------------------|
| org.openmrs.module.emrapi  | emr.admissionEncounterType              | org.openmrs.EncounterType |
| org.openmrs.module.emrapi  | emr.transferWithinHospitalEncounterType | org.openmrs.EncounterType |
| org.openmrs.module.emrapi  | emr.exitFromInpatientEncounterType      | org.openmrs.EncounterType |

### Dispositions

The EMR API module supports _requests_ for admission, discharge, and transfer via a specific set of "disposition" observations.
One must set up and add EMR API mappings to concepts in their system to indicate which concepts should represent specific dispositions.
These include:

| Concept Source Name       | Concept Term Name          | Concept Class | Concept Datatype |
|---------------------------|----------------------------|---------------|------------------|
| org.openmrs.module.emrapi | Disposition Concept Set    | ConvSet       | N/A              |
| org.openmrs.module.emrapi | Disposition                | Question      | Coded            |
| org.openmrs.module.emrapi | Admission Location         | Question      | Text             |
| org.openmrs.module.emrapi | Internal Transfer Location | Question      | Text             |
| org.openmrs.module.emrapi | Date of Death              | Question      | Date             |

* The `Disposition Concept Set` should contain the other concepts above as set members
* The `Disposition` concept should contain concepts for the specific dispositions supported.  These concepts do not need any specific mappings, 
  but will be configured within the `Disposition Config`, described below.  Typical examples of concepts to include are:
  * `Admit to Hospital -> Misc; N/A`
  * `Internal Transfer -> Misc; N/A`
  * `Discharge from Hospital -> Misc; N/A`
* The `Admission Location`, `Internal Transfer Location`, and `Date of Death` concepts represent additional observations typically collected within
  the same obs group as the disposition, in order to collect associated information related to the chosen disposition.

Once disposition concepts are configured, one needs to create a `Disposition Config`.  This is a json file which communicates with the EMR API
module to indicate which dispositions are available in the system, what associated questions/observations are collected for each, and
what type of ADT action is each associated with.  There are typically 2 main ways of setting the disposition config:

* Programmatically calling `dispositionService.setDispositionConfig(String path)`, where path is something like 
  `file:configuration/pih/pih-dispositions-haiti.json`
* Using initializer to load the disposition config, by placing a single json file (with any name) under `.OpenMRS/configuration/dispositions` 

The disposition json file is expected to contain an array of dispositions at the top level.  Each disposition is an object with the following attributes:

* `uuid`: This does not need to be an actual uuid, it is just a unique key/identifier for this particular disposition
* `name`: Typically a message code for how you want this disposition to appear in the UI
* `conceptCode`: A concept uuid or source:mapping reference to the specific concept representing this disposition.  This will be the `valueCoded` of the disposition when selected.
* `type`: (optional, one of ADMIT, TRANSFER, DISCHARGE).  This is what communicates to the EMR API module whether this disposition should represent an ADT request.
* `encounterTypes`: (optional array of uuids).  If specified, this will indicate that this disposition should only be collected in encounters of the given types.
* `excludedEncounterTypes`: (optional array of uuids).  If specified, this will indicate that this disposition should not be collected in encounters of the given types.
* `actions`: (optional array of component names).  If specified, this indicates which Spring components that implement the `DispositionAction` interface should be executed when this disposition is saved
* `additionalObs`: (optional array of objects). If specified, this is means to communicate to the UI what additional observations should be collected with a disposition

Please see the following examples:

* [A relatively simple example](https://github.com/PIH/openmrs-distro-rwandaemr/blob/402a839be5caed9788851e742d95233549f601ee/rwandaemr-content/backend_configuration/dispositions/dispositionConfig.json)
* [A more complex example](https://github.com/PIH/openmrs-config-zl/blob/803e0cb09b4b782402ece3f01fe6d6b1d5655357/configuration/pih/pih-dispositions-haiti-inpatient.json)

### Admission Decisions

In order to account for circumstances where a request for Admission, Discharge, or Transfer was initiated via a disposition, but the subsequent
Admission, Discharge, or Transfer encounter is not able to take place, one needs to configure and use an `Admission Decision` obs to indicate this.

The configuration needed to set this up would be to map 2 concepts as follows:

| Concept Source Name       | Concept Term Name  | Concept Class | Concept Datatype |
|---------------------------|--------------------|---------------|------------------|
| org.openmrs.module.emrapi | Admission Decision | Question      | Coded            |
| org.openmrs.module.emrapi | Deny Admission     | Misc          | N/A              |

* The `Deny Admission` concept should be added as an answer to the `Admission Decision` question


# API

## account

* AccountDomainWrapper - wrapper for Person + User + Provider with methods for getting and manipulating account-related data
* AccountService - CRUD operations for AccountDomainWrapper and opinionated methods for getting roles and privileges
* AccountValidator - validates an AccountDomainWrapper
* ProviderIdentifierGenerator - interface for generating a provider identifier

## adt

* Provides higher level APIs around Admission, Discharge, and Transfer
* These are based on the "Disposition" configuration in DispositionService / DispositionDescriptor and metadata in EmrApiProperties
* A/D/T based around specific encounter type and optional form
* Provides an implementation of automatically creating visits and assigning encounters to visits with appropriate visit types, if no visit is associated with the encounter
* Provides an implementation of closing "stale" visits and a scheduled task to automate this
* Provides additional service methods to manage visits, ensure active visit, deal with overlapping visits, manage visit locations, enter retrospective visits
* Provides implementations for merging patients and merging visits
* Provides APIs for creating checkin encounters, adt encounters, and identify and get inpatient visits

## concept

* Provides additional service methods for getting and searching for concepts

## db

* Various utility classes for dealing with DB and Hibernate queries and access and DAO implementations in support of other services

## descriptor

* Utilities for working defining concept sets and members and creating and manipulating obs groups

## diagnosis

* Diagnosis and CodedOrFreeTextAnswer wrapper classes to represent a diagnosis as a concept set / obs group 
* Deprecated services and utility classes (is core as of 2.2) related to Diagnosis

## disposition

* Concept Set / Obs Group specification for modeling a disposition
* Configuration relating disposition to Admission / Discharge / Transfer
* Json parsing and handling specification for reading in a dispositionConfig.json that determines how disposition obs behave 
* Services and DispositionAction interface and implementations for performing operations as a result of a disposition

## domainwrapper

* Utility classes for creating and manaing domain wrapper classes

## encounter

* EncounterTransaction that provides a json model object for encounter, obs, order, diagnosis, disposition, providers, and additional extensions and context
* Mappers to convert to/from data and EncounterTransaction / json
* Matcher interface and base implementations to find an existing Encounter given a set of parameters
* Processor interface for reading and saving Encounter and EncounterTransaction
* Service methods to leverage the other entities to enable saving/finding/retrieving encounters based on the EncounterTransaction construct
* Logic for handling evolution of Orders through to OpenMRS 1.9

## event

* Basic interface and implementation for publishing events with the event module whenever a user views a patient
* Event listener on patient view events to track in the user's property of emrapi.lastViewedPatientIds

## exitfromcare

* Service with methods to close/reopen programs, visits, patient death information

## merge

* Interfaces for PatientMergeAction and VisitMergeAction, used within the merge methods in the adt package
* Should really have this and the service methods in the adt package together

## metadata

* Utility classes for mds import specifications, likely no longer used

## patient

* PatientDomainWrapper - convenience methods around Patient and related tables, including primary identifier, telephone number, unknown patient, and test patient 
* EmrPatientProfileService - allows associating a patient with a PersonImage
* EmrPatientService - methods to find patients by visit location or primary identifier
* (reporting module) definitions and evaluators for getting patient Primary Identifier

## person

* Services and convenience entities for saving person images to a configurable directory

## test

* Convenience classes for constructing concepts, concept data types, and obs

## utils

* Utility classes / functions in support of the other packages as needed

## visit

* Service and parameter objects to find visits and return associated EncounterTransactions in that visit
* Service methods to get diagnoses obs and patients with particular diagnoses
* VisitDomainWrapper - convenience wrapper to get info around active / open status, encounters contained, diagnoses, dispositions, ADT status
