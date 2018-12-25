package locus.api

import locus.api.manager.LocusMapManager
import locus.api.mapper.DataMapper
import locus.api.mapper.GeocacheConverter
import locus.api.mapper.GeocacheLogConverter
import locus.api.mapper.ImageDataConverter
import locus.api.mapper.TrackableConverter
import locus.api.mapper.WaypointConverter
import org.koin.dsl.module.module

/**
 * Created by Arcao on 08.12.2018.
 */
internal val locusMapApiModule = module {
    single {
        LocusMapManager(get())
    }
    single {
        TrackableConverter()
    }
    single {
        WaypointConverter()
    }
    single {
        ImageDataConverter()
    }
    single {
        GeocacheLogConverter(get())
    }
    single {
        GeocacheConverter(get(), get(), get(), get(), get(), get())
    }
    single {
        DataMapper(get(), get(), get())
    }
}